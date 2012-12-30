package parser;

/**
 * Created by User: ting
 * Date: 9/19/12
 * Time: 9:58 PM
 * A recursive descent regex parser based on the following grammar
 * <regex> ::= <term> '|' <regex> |  <term>

 * <term> ::= { <factor> }

 * <factor> ::= <base> { '*' }
 *              | <base> { '+' }
 *              | <base> { '?' }
 *              | <base> { '{n}' }
 *              | <base> { '{n,}' }
 *              | <base> { '{n,m}' }
 *
 * <base> ::= <char>
 *              |  '\' <char>
 *              |  '(' <regex> ')
 *              |  <charClass>
 *
 * <charClass> ::=  '[' { <charGroup> } ']'
 *                 
 * <charGroup> ::= <char> |
 *               <charRange> |
 *               <charClass>
 * <charRange> ::= <char> '-' <char>
 */
public class RegexParser {
    private String input;
    
    public static void main(String[] args){
        RegexParser parser = new RegexParser("[d-g[1-9]]");
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

        while (more() && (peek() == '*' ||
                          peek() == '?' ||
                          peek() == '+' ||
                          peek() == '{')) {
            base = eatRepetition(base) ;
        }
        return base;
    }

    /**
     * 
     * @param base the internal regex of the returned repetition 
     * @return the repetition with min,max 
     */
    private Repetition eatRepetition(Regex base) {
        char c = peek();
        switch(c){
            case '*':
                eat('*');
                return new Repetition(base,0,Integer.MAX_VALUE);
            case '+':
                eat('+');
                return new Repetition(base,1,Integer.MAX_VALUE);
            case '?':
                eat('?');
                return new Repetition(base,0,1);
            default:
                eat('{');
                int min = eatNum();
                int max;
                if(peek() == '}')
                    max = min; 
                else{
                    eat(',');
                    if(peek() == '}'){
                        max = Integer.MAX_VALUE;
                    }else{
                        max = eatNum();
                    }
                }
                eat('}');
                return new Repetition(base,min,max);
        }
    }

    /**
     * 
     * @return the number which is the prefix of input
     */
    private int eatNum() {
        int r = 0;
        if('0' > peek() || peek() > '9')
            throw new RuntimeException("Not a number within a {n,m} repetition structure: " + peek());
        while(peek() >= '0' && peek() <='9'){
            r = r * 10 + Integer.parseInt(String.valueOf(next()));
        }
        return r;
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
            
            case '[':
                eat('[');
                CharClass cg = charClass();
                eat(']');
                return cg;

            default:
                return new Primitive(next()) ;
        }
    }

    /**
     *
     * @return the node representing the strings between '[' and ']' of a char class
     */
    private CharClass charClass() {
        CharClass charClass = new CharClass();
        char c;
        while((c=peek()) != ']'){
            if(c != '['){
                if(input.length() > 1 && input.charAt(1) == '-'){ //char range
                    char from = next();
                    eat('-'); // consume -
                    if(!more())
                        throw new RuntimeException("Incomplete character range" + input);
                    char to = next();
                    for(char tmp = from; tmp <= to; tmp++){
                        charClass.add(new Primitive(tmp));
                    }
                }
                else{ // a single character option
                    charClass.add(new Primitive(next()));
                }
            }
            else{ //nested char classes
               eat('['); //consume [
               charClass.add(charClass().condense()); //recursively add the nested structure
               eat(']');
            }
        }
        return charClass;
    }

}
