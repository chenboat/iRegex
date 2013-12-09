package query.NFA;

import parser.Choice;
import parser.Primitive;
import parser.Repetition;
import parser.Sequence;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/28/13
 * Time: 10:02 PM
 */
public class QueryConversionSideEffect implements ConversionSideEffect {
    private HashMap<Primitive,Transition> map;

    public HashMap<Primitive, Transition> getMap() {
        return map;
    }

    public QueryConversionSideEffect()
    {
        map = new HashMap<Primitive, Transition>();
    }

    @Override
    public void onPrimitiveNode(NFA nfa, Primitive p) {
        assert nfa.getInitState().getTransitions().size() == 1;
        map.put(p,nfa.getInitState().getTransitions().get(0));
    }

    @Override
    public void onSequenceNode(NFA nfa, Sequence s) {
    }

    @Override
    public void onChoiceNode(NFA nfa, Choice c) {
    }

    @Override
    public void onRepetitionNode(NFA nfa, Repetition r) {
    }
}
