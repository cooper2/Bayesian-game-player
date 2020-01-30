
/**
 * 
 * @author Cooper
 * @version 1
 */
public class TestBayesianProbabilities 
{
    /**
     * Constructor for objects of class Test
     */
    public TestBayesianProbabilities()
    {
             
    }
    
    // instance variables - replace the example below with your own
    BayesianAI zero = new BayesianAI();
    /**
     * Test method
     * expected terminal output is P(A) = 0.5 for player B and player C
     */
    public void test()
    {
        BayesianAI zero = new BayesianAI();
        zero.get_status("A", "ABCDE", "??", 1, 0);
        zero.get_ProposedMission("B", "BC");
        zero.get_Traitors(1);   
        zero.get_status("A", "ABCDE", "??", 2, 0);
        zero.get_ProposedMission("B", "BA");
        zero.get_Traitors(1);  
        


    }

}
