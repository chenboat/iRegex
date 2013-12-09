package query.NFA;

import parser.Choice;
import parser.Primitive;
import parser.Repetition;
import parser.Sequence;

/**
 * Created with IntelliJ IDEA.
 * Author: Ting Chen
 * Date: 11/28/13
 * Time: 9:41 PM
 */
public interface ConversionSideEffect {
    public void onPrimitiveNode(NFA nfa, Primitive p);
    public void onSequenceNode(NFA nfa, Sequence s);
    public void onChoiceNode(NFA nfa, Choice c);
    public void onRepetitionNode(NFA nfa, Repetition r);
}
