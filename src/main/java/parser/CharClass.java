package parser;

/**
 * Created by User: ting
 * Date: 12/29/12
 * Time: 3:42 PM
 *
 * This class corresponds to either [abc] or [a-zA-Z].
 * 
 */
public class CharClass extends Regex{
    Regex choice;
    
    public CharClass(){
    }

    /**
     * 
     * @param p add one more character to the union represented by the choice
     *  If the current choice is null, let choice = p
     *  Otherwise
     *          Replace the current choice node with
     *              choice (new)
     *               /   \
     *     choice(old)   p  
     */
    public void add(Regex p){
        if(choice == null)
            choice = p;
        else
            choice = new Choice(choice,p);
    }
            
    @Override
    public Regex condense() {
        return choice;
    }
}
