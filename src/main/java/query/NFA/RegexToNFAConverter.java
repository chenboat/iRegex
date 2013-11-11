package query.NFA;

import parser.*;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/10/13
 * Time: 4:58 PM
 */
public class RegexToNFAConverter {
    public static NFA convert(Regex r) throws IllegalStateException
    {
        if(r instanceof Primitive)
        {
            return new NFA(new NFAState(r.toString()));
        }else if(r instanceof Sequence)
        {
            Sequence sequence = (Sequence)r;
            NFA left = convert(sequence.getLeftRegex());
            NFA right = convert(sequence.getRightRegex());

            // Use the init state of left nfa as the init state of the combined nfa
            NFA combined = new NFA(left.getInitState());
            // Use the final states of right nfa as the final states of the combined nfa
            combined.setFinalStates(right.getFinalStates());

            // Make transitions from each final state of left to the init state of right nfa
            for(NFAState s : left.getFinalStates())
            {
                s.addTransition(new Transition(s, right.getInitState()));
            }
            return combined;
        }else if(r instanceof Choice)
        {
            Choice c = (Choice)r;
            NFA left = convert(c.getLeftRegex());
            NFA right = convert(c.getRightRegex());
            // Make a new state as the init state of the combined nfa
            NFA combined = new NFA(new NFAState(NFAState.epilsonStr));
            NFAState init = combined.getInitState();

            // add two transitions from the new init state to the init states of left and right nfa
            init.addTransition(new Transition(init,left.getInitState()));
            init.addTransition(new Transition(init,right.getInitState()));

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
        }else if(r instanceof Repetition)
        {
            Repetition repeat = (Repetition)r;
            NFA internal = convert(repeat.getInternal());
            // Make a loop back transitions from all the final states back to the init state
            for(NFAState s: internal.getFinalStates())
            {
                s.addTransition(new Transition(s,internal.getInitState()));
            }
            return internal;
        }else
        {
            throw new IllegalStateException("Regex node type not supported exception" + r);
        }

    }

}
