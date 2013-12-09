package query.NFA;

import parser.Choice;
import parser.Primitive;
import parser.Repetition;
import parser.Sequence;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/28/13
 * Time: 9:50 PM
 */
public class NoConversionSideEffect implements ConversionSideEffect {
    @Override
    public void onPrimitiveNode(NFA nfa, Primitive p) {
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
