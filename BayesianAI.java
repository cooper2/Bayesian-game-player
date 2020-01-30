
import java.util.*;
import java.util.*;
import java.io.*;
import java.*;

/**
 * A Java class for an agent to play in Resistance.
 * Each agent is given a single capital letter, which will be their name for the game.
 * The game actions will be encoded using strings.
 * The agent will be created entirely in a single game, and the agent must maintain its own state.
 * Generally, methods will be used for informing agents of game events (get_ methods), must return in 100ms) or requiring actions (do_ methods, must return in 1000ms).
 * Constructor must return within 1 second.
 * If actions do not meet the required specification, a nominated default action will be recorded.
 * 
 * @author Cooper
 * 
 * References:
 * https://leepike.wordpress.com/2016/02/08/viva-la-resistance-a-resistance-game-solver/
 * **/

public class BayesianAI implements Agent{
    private Scanner scanner;
    private String players = "";
    private String spies = "";
    private String name = "BayesianAI";
    private boolean spy = false;
    private PrintStream out;  
    private Random random;
    private int wins = 0;

    private double chanceOfSpy = 0.0;
    private int numberOfSpies = 0;
    private int numberOfSpiesOnMission = 0;
    private int consecutiveVetoes = 0;
    private int missionNumber = 0;
    private int missionFailures = 0;
    private String missionTeam = "";
    private String missionLeader = "";
    HashMap<Character, Double> suspicions = new HashMap<Character, Double>();
    
    
    private void write(String s)
    {
        System.out.println(s);
    }

    public BayesianAI(){
        random = new Random();
    }

    /**
     * Reports the current status, including players name, the name of all players, the names of the spies (if known), the mission number and the number of failed missions
     * @param name a string consisting of a single letter, the agent's names.
     * @param players a string consisting of one letter for everyone in the game.
     * @param spies a String consisting of the latter name of each spy, if the agent is a spy, or n questions marks where n is the number of spies allocated; this should be sufficient for the agent to determine if they are a spy or not. 
     * @param mission the next mission to be launched
     * @param failures the number of failed missions
     * @return within 100ms
     * */
    public void get_status(String name, String players, String spies, int mission, int failures)
    {
        this.name = name;
        this.players = players;
        this.spies = spies;

        if(spies.indexOf(name)!=-1) 
        {
            spy = true;     // determine if I am a spy
        }

        missionNumber = mission;
        missionFailures = failures; 

        if( mission == 1 )
        {
            char[] playerChars = players.toCharArray();
            numberOfSpies= spies.length();

            // hardcoded for safety
            // chanceOfSpy = (float) numberOfSpies/players.length();
            if( players.length() < 8 )
            {
                chanceOfSpy = 2.0/5.0;
            }
            else
            {
                chanceOfSpy = 3.0/5.0;
            }

            write("chance of spy: " + chanceOfSpy);
            for ( Character c: playerChars )
            {
                suspicions.put(c, chanceOfSpy);
            }
        }

        write("You are: "+name); 
        write("The players in the game are: "+players); 
        write("The Spies are: "+spies); 
        write("The next mission is: "+mission);
        write("So far, "+failures+" missions have failed");

    }

    /**
     * Nominates a group of agents to go on a mission.
     * If the String does not correspond to a legitimate mission (<i>number</i> of distinct agents, in a String), 
     * a default nomination of the first <i>number</i> agents (in alphabetical order) will be reported, as if this was what the agent nominated.
     * @param number the number of agents to be sent on the mission
     * @return a String containing the names of all the agents in a mission, within 1sec
     * */
    public String do_Nominate(int number)
    {
        String nominations = "";    // people we have nominated for the team

        HashMap<Character, Double> suspicionsTemp = new HashMap<Character, Double>();
        suspicionsTemp.putAll(suspicions);  

        HashSet<Character> team = new HashSet<Character>();

        team.add(name.charAt(0));   // always nominate yourself, regardless of team
        suspicionsTemp.remove(name.charAt(0));  // remove self from selection

        if( !spy )  // SPY MEMBER
        {                      
            // find the people least likely to be spies, in descending order 
            for(int i = 0; i<number-1; i++)
            {
                double lowestValue = 1.0;
                char lowestKey = name.charAt(0);

                for( Character player: suspicionsTemp.keySet() )  
                {         
                    if( suspicionsTemp.get(player) < lowestValue )
                    {
                        lowestValue = suspicionsTemp.get(player);
                        lowestKey = player;
                    }

                }

                team.add(lowestKey);
                suspicionsTemp.remove(lowestKey);

            }

        }
        else    // RESISTANCE MEMBER
        {
            // find the people most likely to be spies, who are not actually spies, and nominate them
            for(int i = 0; i<number-1; i++)
            {
                double highestValue = 0.0;
                char highestKey = name.charAt(0);

                for( Character player: suspicionsTemp.keySet() )  
                {         
                    if( suspicionsTemp.get(player) > highestValue && spies.indexOf(player) == -1) // if the person most likely to be a spy is a spy, do not nominate them
                    {
                        highestValue = suspicionsTemp.get(player);
                        highestKey = player;
                    }
                }
                team.add(highestKey);
                //highestValue = 0.0;                       
                suspicionsTemp.remove(highestKey);

            }

        }

        // make sure the team is filled
        if(team.size() < number)
        {
            
            while(team.size() < number)
            {
                char c = players.charAt(random.nextInt(players.length()));
                while(team.contains(c)) 
                {               
                    c = players.charAt(random.nextInt(players.length()));
                }
                team.add(c);
            }
        }

        for(Character c: team)
        {
            nominations += c;
        }
        write("nominations: "+nominations);
        return nominations;

    }

    /**
     * Provides information of a given mission.
     * @param leader the leader who proposed the mission
     * @param mission a String containing the names of all the agents in the mission within 1sec
     **/
    public void get_ProposedMission(String leader, String mission)
    {
        missionTeam = mission;
        missionLeader = leader;

        numberOfSpiesOnMission = 0;
        for(int i = 0; i < missionTeam.length(); i++)       // find the number of spies on the current mission
        {
            char c = missionTeam.charAt(i);
            if( spies.indexOf(c) != -1);
            {
                numberOfSpiesOnMission++;               
                // write("spies.charAt = "+spies.charAt(i) );
                // write("missionTeam.indexOf( spies.charAt(i) )"+ missionTeam.indexOf( spies.charAt(i) ) );
            }
        }

    }

    /**
     * Gets an agents vote on the last reported mission
     * @return true, if the agent votes for the mission, false, if they vote against it, within 1 sec
     * */
    public boolean do_Vote()
    {
        if( spy )   // SPY MEMBER
        {
            if( missionNumber == 1 )            // resistance would never veto on first turn as they do not have any information          
            {
                write("voted for mission");
                return true;                    // vote in favour to avoid suspicion               
            }

            else if( consecutiveVetoes == 4 )   // if there have been 4 consecutive vetoes, a resistance member would never veto
            {
                if( missionFailures == 2 )  
                {
                    write("vetoed mission");
                    return false;   // unless it would win the game, avoid suspicion by voting in favour                    
                }

                write("voted for mission");
                return true; 
                
            }

            // if the number of players on a team match the number of resistance members
            // a resistance member would always veto if they are not on the team as a spy would be gauranteed
            else if ( missionTeam.length() >= players.length()*(2/3) && (missionTeam.indexOf( name ) == -1) )   
            {
                write("vetoed mission");
                return false;   // veto to avoid suspicion               
            }

            if( numberOfSpiesOnMission == 0 )   // if no spies on mission, always veto
            {
                write("vetoed mission");
                return false;               
            }

            write("voted for mission");
            return true;
            

        }
        else    // RESISTANCE MEMBER
        {
            if( missionTeam.indexOf(missionLeader) == -1 )  // always veto if the leader does not include themselves
            {
                write("vetoed mission");
                return false;                
            }
            
            if( missionNumber == 1 || consecutiveVetoes == 4)
            {
                write("voted for mission");
                return true;
            }

            // find the people most likely to be spies, and if they are on the team, veto
            HashMap<Character, Double> suspicionsTemp = new HashMap<Character, Double>();
            suspicionsTemp.putAll(suspicions);  

            HashSet<Character> team = new HashSet<Character>();
            for(int i = 0; i< spies.length(); i++)
            {
                double highestValue = 0.0;
                char highestKey = name.charAt(0);

                for( Character player: suspicionsTemp.keySet() )  
                {         
                    if( suspicionsTemp.get(player) > highestValue ) 
                    {
                        highestValue = suspicionsTemp.get(player);
                        highestKey = player;
                    }
                }
                team.add(highestKey);                      
                suspicionsTemp.remove(highestKey);

            }
            
            for( Character c: team )
            {
                if( missionTeam.indexOf( c.toString() ) != -1 )    // veto if high risk players are on proposed team
                {
                    write("vetoed mission");
                    return false;                    
                }
            }

            write("voted for mission");
            return true;
            
        }
    }

    /**
     * Reports the votes for the previous mission
     * @param yays the names of the agents who voted for the mission
     * @return within 100ms
     **/
    public void get_Votes(String yays)
    {
        if ( yays.length() > players.length()/2 )       // if a majority voted in favour of the mission
        {
            consecutiveVetoes = 0;
        }
        else
        {
            consecutiveVetoes++;
        }

    }

    /**
     * Reports the agents being sent on a mission.
     * Should be able to be inferred from tell_ProposedMission and tell_Votes, but included for completeness.
     * @param mission the Agents being sent on a mission
     * @return within 100ms
     **/
    public void get_Mission(String mission)
    {     
        missionTeam = mission;

        numberOfSpiesOnMission = 0;
        for(int i = 0; i < missionTeam.length(); i++)       // find the number of spies on the current mission
        {
            char c = missionTeam.charAt(i);
            if(spies.indexOf(c) != -1 )
            {
                numberOfSpiesOnMission++;               
            }
        }

    }

    /**
     * Agent chooses to betray or not.
     * @return true if agent betrays, false otherwise, within 1 sec
     **/
    public boolean do_Betray()
    {

        if( missionFailures == 2 )      // always betray if it could make the difference between a win or loss
        {
            return true;
        }

        // if there are too many spies on the mission, or team is too small, betraying will give too much information away
        else if( numberOfSpiesOnMission > 1 || missionTeam.length() == 2 )       
        {
            return false;       // could change this to betraying only if you are the leader, but relies on other spies following this protocol
        }

        else    // if all other conditions have not been met, always betray
        {   
            return true;
        }

    }

    // found at http://stackoverflow.com/questions/4240080/generating-all-permutations-of-a-given-string
    /**
     * Returns all permutations of a given string
     * @param input the input string
     * @returns set a set of all permutations
     **/
    public static Set<String> generatePerm(String input)
    {
        Set<String> set = new HashSet<String>();
        if (input == "")
            return set;

        Character a = input.charAt(0);

        if (input.length() > 1)
        {
            input = input.substring(1);

            Set<String> permSet = generatePerm(input);

            for (String x : permSet)
            {
                for (int i = 0; i <= x.length(); i++)
                {
                    set.add(x.substring(0, i) + a + x.substring(i));
                }
            }
        }
        else
        {
            set.add(a + "");
        }
        return set;
    }

    /**
     * Calculates P( B )
     * @param traitors the number of people on the mission who chose to betray
     * @param givenSpy when calculating P(B | A), this is the name of the given spy
     * @return within 100ms
     **/
    private double calculateProbB(String givenSpy, int traitors)
    {
        // CALCULATE P( B )
        String actions = "";
        int nonTraitors = (missionTeam.length() - traitors);    
        for(int i = 0; i < nonTraitors; i++)
        {
            actions += "0";     // write 0s to represent the resistance members on the mission
        }

        for(int i = 0; i < traitors; i++)                       
        {
            actions += "1";     // write 1s to represent known spies (betrayals) on the mission
        }

        Set<String> possibleTeamLoyalties = generatePerm(actions);      // generate strings to represent possible spy/res allocations for players on mission
        double probabilityOfActionSet = 0.0;

        for( String s: possibleTeamLoyalties )      // go through each team allocation possibility
        {       
            double probOfAllocation = 0.0;
            double totalProbOfAllocation = 0.0;

            for(int i = 0; i < missionTeam.length(); i++)       // find the probability of the player having this allocation
            {
                if(  givenSpy.indexOf(missionTeam.charAt(i)) != -1 )     // if the player is assumed to be a spy for calculating P(B | A)
                {
                    if( s.charAt( i ) == '0')
                    {
                        probOfAllocation = 0.0;
                    }
                    else
                    {
                        probOfAllocation = 1.0;
                    }
                }
                else if( s.charAt( i ) == '0' )    // if player i is resistance in this possible action set
                {
                    probOfAllocation = 1.0 - suspicions.get( missionTeam.charAt(i) );
                }
                else    // player i is a spy in this possible action set
                {
                    probOfAllocation = suspicions.get( missionTeam.charAt(i) );
                }

                if( i == 0 )  // if this is the first iteration, do this to prevent multiplying by zero
                {
                    totalProbOfAllocation = probOfAllocation; 
                }
                else
                {
                    totalProbOfAllocation = totalProbOfAllocation * probOfAllocation; 
                }

            }        
            probabilityOfActionSet += totalProbOfAllocation;
        }

        double pB = probabilityOfActionSet;

        return pB;
    }

    /**
     * Rounds a double to the nearest 2 decimal places.
     * @param number the number to be rounded
     * @return number
     **/
    private double round(double number)
    {
        number = Math.round(number * 100);
        number = number/100;
        return number;
    }

    /**
     * Reports the number of people who betrayed the mission
     * @param traitors the number of people on the mission who chose to betray (0 for success, greater than 0 for failure)
     * @return within 100ms
     **/
    public void get_Traitors(int traitors)
    {
        // Bayes Theorem
        // P( A | B ) = P( B | A ) * P( A ) / P( B )
        //
        // P( A | B ) = probability that player is a spy, given the mission action set
        // P( B | A ) = probability of the mission action set given that the player is a spy
        // P( A ) = probabiblity that the player is a spy
        // P( B ) = probability of the mission action set

        // CALCULATE P( B )
        double pB = calculateProbB("", traitors);

        double totalDifference = 0.0;
        double difference = 0.0;
        double numberNotOnMission = (double) players.length() - missionTeam.length();
        double differencePerPlayer = 0.0;  // the probabilty of being a spy to be deducted from players not on mission

        HashMap<Character, Double> suspicionsTemp = new HashMap<Character, Double>();

        for( Character player: suspicions.keySet() )    // for every player on team, work out their new probability of being a spy
        {
            if( missionTeam.indexOf(player) != -1)      // if the player is on the mission team
            {

                // CALCULATE P( A )
                double pA = suspicions.get(player);

                // CALCULATE P( B | A)
                double pBA = calculateProbB(player.toString(), traitors);

                // CALCULATE P(A | B)
                double pAB = ( pBA * pA ) / pB;
                if( pAB < 0.0 )
                {
                    pAB = 0.0;
                }
                if( pAB > 1.0 )
                {
                    pAB = 1.0;
                }

                difference = pAB - pA;
                totalDifference += difference;                

                suspicionsTemp.put(player, pAB);    // replace old probability with new one
                
            }

        }

        suspicions.putAll(suspicionsTemp);

        for( Character player: suspicions.keySet() )    // for every player not on team, work out their new probability of being a spy
        {
            if( missionTeam.indexOf(player) == -1 && suspicions.get(player) != 1.0) // don't change probability if they are definitely a spy
            {
                double pA = suspicions.get(player);

                double totalDifferencePerPerson = totalDifference/numberNotOnMission;
                totalDifferencePerPerson = round(totalDifferencePerPerson);
   
                double newProbA = pA - (totalDifferencePerPerson);
                if( newProbA < 0.0 )
                {
                    newProbA = 0.0;
                }

                suspicionsTemp.put(player, newProbA);   // replace old probability with new one

            }
        }
        suspicions.putAll(suspicionsTemp);
        for( Character player: suspicions.keySet() )  
        {         
            write("suspicion probability of " + player);
            write("is: " + suspicions.get(player) );

        }

    }

    /**
     * Optional method to accuse other Agents of being spies. 
     * Default action should return the empty String. 
     * Convention suggests that this method only return a non-empty string when the accuser is sure that the accused is a spy.
     * Of course convention can be ignored.
     * @return a string containing the name of each accused agent, within 1 sec 
     * */
    public String do_Accuse()
    {
        String accusation = "";
        return accusation; 
    }

    /**
     * Optional method to process an accusation.
     * @param accuser the name of the agent making the accusation.
     * @param accused the names of the Agents being Accused, concatenated in a String.
     * @return within 100ms
     * */
    public void get_Accusation(String accuser, String accused)
    {
        wins();        
    }
    
    public int wins()
    {

        if(spy)
        {
            if( missionFailures ==  3);
            wins++;
            return wins;
        }
        else
        {
            if( missionFailures ==  3);
            return wins;
        }
        
    }

}