package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 9:58 PM
 * A recursive descent regex parser based on the following grammar
 * <regex> ::= <term> '|' <regex> |  <term>

 * <term> ::= { <factor> }

 * <factor> ::= <base> { '*' }

 * <base> ::= <char>
    |  '\' <char>
    |  '(' <regex> ')
 */
public class RegexParser {
    private String input;
    
    public static void main(String[] args){
        RegexParser parser = new RegexParser("(abdfd|cf)*ddfd");
        Regex regex = parser.parse();
        System.out.println(regex);
    }
    public RegexParser(String input) {
        this.input = input;
    }

    public Regex parse () {
        Regex raw = regex();
        return condense(raw);
    }

    /**
     * @param raw a binary raw regex
     * @return recursively combine all char nodes of a sequence node into a single string
     */
    private Regex condense(Regex raw) {
        return raw.condense();
    }

    /* Recursive descent parsing internals. */

    private char peek() {
        return input.charAt(0) ;
    }
    private void eat(char c) {
        if (peek() == c)
            this.input = this.input.substring(1) ;
        else
            throw new
            RuntimeException("Expected: " + c + "; got: " + peek()) ;

    }
    private char next() {
        char c = peek() ;
        eat(c) ;
        return c ;
    }

    private boolean more() {
        return input.length() > 0 ;
    }


    /* Regular expression term types. */

    private Regex regex() {
        Regex term = term() ;

        if (more() && peek() == '|') {
            eat ('|') ;
            Regex regex = regex() ;
            return new Choice(term,regex) ;
        } else {
            return term ;
        }

    }
    private Regex term() {
        Regex factor = Regex.blank ;

        while (more() && peek() != ')' && peek() != '|') {
            Regex nextFactor = factor() ;
            factor = new Sequence(factor,nextFactor) ;
        }

        return factor ;

    }
    private Regex factor() {
        Regex base = base() ;

        while (more() && peek() == '*') {
            eat('*') ;
            base = new Repetition(base) ;
        }

        return base ;


    }
    private Regex base() {
        switch (peek()) {
            case '(':
                eat('(') ;
                Regex r = regex() ;
                eat(')') ;
                return r ;

            case '\\':
                eat ('\\') ;
                char esc = next() ;
                return new Primitive(esc) ;

            default:
                return new Primitive(next()) ;
        }
    }

}
