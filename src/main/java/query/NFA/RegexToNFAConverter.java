package query.NFA;

import parser.*;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/10/13
 * Time: 4:58 PM
 */
public class RegexToNFAConverter {

    public static NFA convert(Regex r) throws IllegalStateException
    {
        return convert(r,new NoConversionSideEffect());
    }

    public static NFA convert(Regex r, ConversionSideEffect sideEffect)
    {
        if(r instanceof Primitive)
        {
            NFAState from = new NFAState();
            NFAState to = new NFAState();
            from.addTransition(new Transition(from,to,r.toString()));
            NFA nfa = new NFA(from);
            nfa.addFinalState(to);
            sideEffect.onPrimitiveNode(nfa,(Primitive)r);
            return nfa;
        }else if(r instanceof Sequence)
        {
            Sequence sequence = (Sequence)r;
            NFA left = convert(sequence.getLeftRegex(), sideEffect);
            NFA right = convert(sequence.getRightRegex(), sideEffect);

            // Use the init state of left nfa as the init state of the combined nfa
            NFA combined = new NFA(left.getInitState());
            // Use the final states of right nfa as the final states of the combined nfa
            combined.setFinalStates(right.getFinalStates());

            // Make transitions from each final state of left to the init state of right nfa
            for(NFAState s : left.getFinalStates())
            {
                s.addTransition(new Transition(s, right.getInitState(),Transition.EPSILON_STR));
            }
            sideEffect.onSequenceNode(combined, sequence);
            return combined;
        }else if(r instanceof Choice)
        {
            Choice c = (Choice)r;
            NFA left = convert(c.getLeftRegex(), sideEffect);
            NFA right = convert(c.getRightRegex(), sideEffect);
            NFA unioned = unionTwoNFAs(left,right);
            sideEffect.onChoiceNode(unioned,c);
            return unioned;

        }else if(r instanceof Repetition)
        {
            Repetition repeat = (Repetition)r;

            NFAState emptyState = new NFAState();
            NFA emptyNFA = new NFA(emptyState);
            emptyNFA.addFinalState(emptyState);

            NFA internal = convert(repeat.getInternal(), sideEffect);
            // Make a loop back transitions from all the final states back to the init state
            for(NFAState s: internal.getFinalStates())
            {
                s.addTransition(new Transition(s,internal.getInitState(),Transition.EPSILON_STR));
            }
            NFA unioned = unionTwoNFAs(emptyNFA,internal);
            sideEffect.onRepetitionNode(unioned,repeat);
            return unioned;
        }else
        {
            throw new IllegalStateException("Regex node type not supported exception" + r);
        }

    }

    private static NFA unionTwoNFAs(NFA left, NFA right)
    {
        // Make a new state as the init state of the combined nfa
        NFA combined = new NFA(new NFAState());
        NFAState init = combined.getInitState();

        // add two transitions from the new init state to the init states of left and right nfa
        init.addTransition(new Transition(init,left.getInitState(), Transition.EPSILON_STR));
        init.addTransition(new Transition(init,right.getInitState(), Transition.EPSILON_STR));

        // the final states of both left and right nfas are the final states
        for(NFAState s: left.getFinalStates())
        {
            combined.addFinalState(s);
        }
        for(NFAState s: right.getFinalStates())
        {
            combined.addFinalState(s);
        }
        return combined;
    }

}
