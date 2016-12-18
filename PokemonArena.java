//PokemonArena.java
//Pokemon Arena game that lets you fight pokemon
import java.io.*;
import java.util.*;

public class PokemonArena{
	//DATA MANAGEMENT METHODS
	public static boolean isNumeric(String str){
		//checks if something is an +integer
		return str.matches("-*\\d+");
	}
	public static String getValidInput(String query,Scanner sc,int min, int max){
		//overrides to make exceptions optional
		return getValidInput(query,sc,min,max,new String[0]);
	}
	public static String getValidInput(String query,Scanner sc,int min,int max,String[] excepts){
		//checks if the Input is integer (or within exceptions) and returns the valid input
		System.out.println(query); //prints the question
		String input = sc.nextLine().toLowerCase();
		if(isNumeric(input)){
			int intInput = Integer.parseInt(input); //int version of input
			if(intInput >= min && intInput <= max) return input; //input is valid within bounds so we return input
			System.out.println("Out of bounds."); //otherwise out of bounds and queries again
			return getValidInput(query,sc,min,max,excepts);
		} else if(Arrays.asList(excepts).contains(input)){
			return input; //exceptions are valid so we return the input
		} else{
			//not an exception or an int
			System.out.println("Enter an integer please."); //not an integer so we recall function to try and get int
			return getValidInput(query,sc,min,max,excepts); //recalls function
		}
	}
	public static Pokemon[] loadPokemon() throws IOException{
		//loads pokemon, returns a Pokemon array
		Scanner file = new Scanner(new BufferedReader(new FileReader("pokemon.txt"))); //file
		int numPkmn = Integer.parseInt(file.nextLine()); //gets the number of pokemon
		Pokemon[] allPkmn = new Pokemon[numPkmn];
		int i = 0; //index of the pokemon to add
		while(file.hasNextLine()){
			String[] line = file.nextLine().trim().split(",");
			allPkmn[i] = new Pokemon(line); //creates pokemon from line
			i++;
		}
		file.close();
		return allPkmn;
	}
	
	//GAMEPLAY/UI/INPUT METHODS
	public static void makeUserEnter(){
		//forces user to enter to continue;
		Scanner sc = new Scanner(System.in);
		System.out.println("[Enter to continue]");
		sc.nextLine();
	}
	public static boolean confirm(String msg,Scanner sc){
		//asks user to confirm if they want to commit something, returns a boolean
		while(true){
			System.out.println(msg);
			String input = sc.nextLine().toLowerCase();
			if(input.equals("y"))
				return true; //confirmed
			else if(input.equals("n"))
				return false; //denied
			else{
				System.out.println("Enter Y/N please"); //requests Y or N
				continue;
			}
		}
	}
	public static void getPokemonSelection(ArrayList<Pokemon> userPkmn, ArrayList<Pokemon> otherPkmn, Pokemon[] allPkmn, Scanner sc){
		//has the user select their 4 pokemon
		while(userPkmn.size() < 4){
			String input = getValidInput("Select a pokemon [Enter # next to name, or info for info]:",sc,1,allPkmn.length,new String[]{"info","quit"}); //gets vald input
			if(input.equals("info")){
				reqInfo(allPkmn,sc); //handles info query
				makeUserEnter();
				printPokemon(allPkmn); //reprints all Pokemon
			} else if(input.equals("quit")){
				System.out.println("Good bye! SAYONARA!"); //exits game
				System.exit(0);
				break;
			} else{
				//adds the selected pokemon
				int selected = Integer.parseInt(input);
				if(userPkmn.contains(allPkmn[selected-1])){
					//if we already selected that pokemon we can't select it again so we continue
					System.out.println("You already selected that Pokemon");
					continue;
				}
				//add to userPkmn remove from otherPkmn
				userPkmn.add(allPkmn[selected-1]);
				otherPkmn.remove(allPkmn[selected-1]);
				System.out.println(allPkmn[selected-1].getName() + " added.");
			}
		}
	}
	
	public static void reqInfo(ArrayList<Pokemon> allPkmn, Scanner sc){
		//requests info from user about a pokemon
		printPokemon(allPkmn); //prints all pokemon
		String inp = getValidInput("Enter a pokemon's number to get info about it:",sc,1,allPkmn.size(),new String[]{"quit"});
		if(inp.equals("quit")){
			System.out.println("Query cancelled.");
		} else{
			int selected = Integer.parseInt(inp); //integer form of user's input
			selected--; //brings selected to range of 0 - allPkmn.length-1
			System.out.println(allPkmn.get(selected)); //prints pokemon info
		}
	}

	public static void reqInfo(Pokemon[] allPkmn, Scanner sc){
		//override with primitive array
		reqInfo(new ArrayList<Pokemon>(Arrays.asList(allPkmn)),sc);
	}

	public static int pickAttack(Pokemon ally, Scanner sc){
		//picks an attack for ally to use
		System.out.println(ally.getMovesAsString());
		String line = getValidInput("Select a move [Enter move number, quit to cancel]: ",sc,1,ally.getNumMoves(),new String[]{"quit"}); //user's input
		if(line.equals("quit")){
			return -1;
		}
		int sel = Integer.parseInt(line); //user's selection
		sel--;
		if(!ally.canUseAttack(sel)){
			System.out.println("Not enough energy to use that move"); //tries to get another move if this one is unavailable
			makeUserEnter();
			return pickAttack(ally,sc); //repick an attack
		}
		return sel;
	}
	public static void attack(int sel, Pokemon atker, Pokemon target, Scanner sc){
		//attacks with atker to target with selected move (sel)
		if(sel == -1 && atker.getHp() > 0){
			//For enemies only, means no available moves to use so they pass, and if they have no hp no atk
			System.out.println(atker.getName() + " passes.");
		} else if(atker.getStunned() && atker.getHp() > 0){
			System.out.println(atker.getName() + " is stunned!"); //we don't attack if atker is stunned
		} else if(atker.getHp() > 0){
			//if atker is alive we attack with them
			System.out.printf("%s used %s on %s!\n",atker.getName(),atker.getMoveName(sel),target.getName());
			//previous statuses so I can print more precisely what the move did
			int dmg = atker.attack(sel,target); //damage done
			if(atker.getMissed()){
				System.out.println(atker.getName() + " missed!");
			}
			if(!atker.getMissed()){
				//if ally did not miss we print damage
				if(atker.getEffectiveness() == Pokemon.SUPER_EFFECTIVE) System.out.println("It's super effective!");
				if(atker.getEffectiveness() == Pokemon.NOT_EFFECTIVE) System.out.println("It's not very effective...");
				System.out.println(target.getName() + " took " + dmg + " damage!");
			}
			if(atker.getTimesHit() > 0){
				//if we didn't miss and we have more times hit then we had a wild storm attack
				System.out.println("Hit " + atker.getTimesHit() + " time(s)!"); //prints time hit
			}
		}
	}
	public static int retreat(Pokemon ally, Pokemon enemy, ArrayList<Pokemon> userPkmn, Scanner sc){
		//retreats and returns the newAlly (-1 otherwise)
		if(ally.getStunned()){ 
			//can't retreat if stunned
			System.out.println(ally.getName() + " is stunned!");
			return -1;
		} else{
			if (confirm("Do you really want to retreat? [Y/N]",sc)){ //do not retreat if user does not want to
				Pokemon newAlly = getAllyPokemon(userPkmn,sc); //gets a new ally
				attack(getEnMove(enemy),enemy,newAlly,sc); //performs an attack on ally
				for(Pokemon p: userPkmn) p.endTurn(); //applies turn end
				enemy.endTurn();
				return userPkmn.indexOf(newAlly); //returns ally's index
			}
			return -1;
		}
	}
	public static void pass(Pokemon ally, Pokemon enemy, ArrayList<Pokemon> userPkmn, Scanner sc){
		//passes the turn
		if (confirm("Do you really want to pass? [Y/N]",sc)){//if user wants to pass
			System.out.println(ally.getName() + " passes.");
			attack(getEnMove(enemy),enemy,ally,sc); //performs an attack on ally
			for(Pokemon p: userPkmn) p.endTurn(); //applies turn end
			enemy.endTurn();
		}
	}
	public static int getEnMove(Pokemon enemy){
		//Enemy's AI and generates a random move for enemy. -1 means pass
		ArrayList<Integer> enUsableMoves = new ArrayList<Integer>(); //enemy's usable moves (in indexes)
		for(int i = 0;i < enemy.getNumMoves();i++){
			if(enemy.canUseAttack(i)){
				//if enemy can use move we add to enUsableMoves
				enUsableMoves.add(i);
			}
		}
		if(enUsableMoves.size() == 0){
			//enemy passes
			return -1;
		}
		return enUsableMoves.get((int)(Math.random()*enUsableMoves.size())); //returns index of random move
	}
	
	public static Pokemon getRandomPkmn(ArrayList<Pokemon> pkmn, Scanner sc){
		//gets a random enemy to fight
		//the list is shuffled so the first one will do
		Pokemon enemy = pkmn.get(0);
		System.out.println(enemy.getName() + " is your opponent");
		makeUserEnter();
		System.out.println(enemy); //prints info
		makeUserEnter();
		return enemy;
	}
	
	public static Pokemon getAllyPokemon(ArrayList<Pokemon> userPkmn, Scanner sc){
		//queries user for an ally pokemon
		System.out.println("Select an ally: ");
		while(true){
			printPokemon(userPkmn);
			String line = getValidInput("Choose an awake pokemon [info for info]: ",sc,1,userPkmn.size(),new String[]{"info"});
			if(line.toLowerCase().equals("info")){
				reqInfo(userPkmn,sc);
				continue;
			}
			int selected = Integer.parseInt(line);
			selected--; //brings index to the right scale (0 - 3)
			//makes sure pokemon is within bounds of allies
			Pokemon selPkmn = userPkmn.get(selected);
			if(selPkmn.getHp() <= 0){
				System.out.println("That pokemon has fainted!");
				continue;
			} else{
				//selects the ally
				System.out.println(userPkmn.get(selected).getName() + ", I choose you!");
				return userPkmn.get(selected);
			}
		}
	}
	//PRINT METHODS
	public static void printStart(){
		//prints start
		System.out.println(" ______  _______  __  __  _______  _______  _______  _______      _______  ______  _______  _______  _______ \n"+
"|   __ \\|       ||  |/  ||    ___||   |   ||       ||    |  |    |   _   ||   __ \\|    ___||    |  ||   _   |\n"+
"|    __/|   -   ||     < |    ___||       ||   -   ||       |    |       ||      <|    ___||       ||       |\n"+
"|___|   |_______||__|\\__||_______||__|_|__||_______||__|____|    |___|___||___|__||_______||__|____||___|___|");
		System.out.println("   __  _    ,      _    ,   __  _    __  ___ _    ,   __  _    __\n"+
"  /  )' )  /      ' )  /   / ')' )  /      /' )  /   / ')' )  /  \n"+
" /--<  /  /    o   /  /   /  /  /  /      /  /--/   /  /  /  /   \n"+
"/___/_(__/_       (__/_  (__/  (__/      /__/  (_  (__/  (__/    \n"+
"       //    o     //                                            \n"+
"      (/          (/");
	}
	public static void printHelp(){
		//prints help
		System.out.println("QUIT - leaves the game\n"
			+ "INFO - displays data about any pokemon in the game\n"
			+ "INFO A - displays data about any alive ally\n"
			+ "INFO E - displays data about any alive enemy\n"
			+ "ATTACK - attacks using one the selected pokemon's moves\n"
			+ "RETREAT - switches out pokemon with another awake pokemon\n"
			+ "PASS - passes the turn");
	}
	
	public static void printPokemonData(Pokemon a, Pokemon b){
		//prints pokemon data shown during each turn
		System.out.printf("%-15s%54s\n","ALLY","ENEMY");
		System.out.printf("%-15s %-10s HP: %-3d/%-3d Energy: %-2d%15s%-15s %-10s HP: %-3d/%-3d Energy: %-2d\n",a.getName(),a.getType(),a.getHp(),a.getMaxHp(),a.getEnergy(),"",b.getName(),b.getType(),b.getHp(),b.getMaxHp(),b.getEnergy());
	}
	
	public static void printPokemon(Pokemon[] pkmn){
		//prints all Pkmn in pkmn (with numbers next to name)
		printPokemon(new ArrayList<Pokemon>(Arrays.asList(pkmn))); //overrides to ArrayList
	}
	public static void printPokemon(ArrayList<Pokemon> pkmn){
		//prints all Pkmn in pkmn (with numbers next to name)
		for(int i = 0;i < pkmn.size();i++){
			System.out.printf("%2d. %-12s %-8s HP: %-3d/%-3d\n",i+1,pkmn.get(i).getName(),pkmn.get(i).getType(),pkmn.get(i).getHp(),pkmn.get(i).getMaxHp());
		}
	}
	public static void main(String[] args) throws IOException{
		Scanner sc = new Scanner(System.in); //the iostream
		//LOADS ALL POKEMON
		Pokemon[] allPkmn = loadPokemon(); //the pkmn
		ArrayList<Pokemon> userPkmn = new ArrayList<Pokemon>(); //user's pokemon
		ArrayList<Pokemon> otherPkmn = new ArrayList<Pokemon>(Arrays.asList(allPkmn)); //enemy pokemon (removes ally pokemon later)
		
		//GAME START
		printStart();
		makeUserEnter();
		printPokemon(allPkmn); //prints the name of all Pokemon
		//POKEMON SELECTION
		getPokemonSelection(userPkmn,otherPkmn,allPkmn,sc); //gets pokemon selections from user
		System.out.println("Game starting."); //game start text
		Collections.shuffle(otherPkmn); //shuffles enemy
		Pokemon enemy = getRandomPkmn(otherPkmn,sc); //gets a random enemy from otherPkmn
		Pokemon ally = getAllyPokemon(userPkmn,sc); //asks user for their selection of pokemon to use
		
		boolean running = true;
		//MAIN GAME LOOP
		while(running){
			//HANDLES USER INPUT
			printPokemonData(ally,enemy); //prints only the very basic data for ally and enemy
			System.out.println("What will you do? [Help for more commands]: ");
			String comm = sc.nextLine().toLowerCase(); //the command
			
			//HANDLES DIFFERENT COMMANDS
			if(comm.equals("quit")){
				System.out.println("Good bye! SAYONARA!");
				running = false;
			} else if(comm.equals("help")){
				printHelp();
				makeUserEnter();
			} else if(comm.equals("info")){
				reqInfo(allPkmn,sc);
				makeUserEnter();
			} else if(comm.equals("info a")){
				reqInfo(userPkmn,sc);
				makeUserEnter();
			} else if(comm.equals("info e")){
				reqInfo(otherPkmn,sc);
				makeUserEnter();
			} else if(comm.equals("attack")){
				if(ally.getStunned()){
					System.out.println(ally.getName() + " is stunned!"); //can't attack if we're stunned
					continue;
				}
				//requests a move from user
				int sel = pickAttack(ally,sc);
				if (sel == -1){
					//attack cancelled
					System.out.println("Attack cancelled");
				} else{
					//now we determine who goes first
					if(Math.random() >= 0.5){
						//ally attacks first
						attack(sel,ally,enemy,sc);
						attack(getEnMove(enemy),enemy,ally,sc); //performs an attack on ally
					} else{
						//ally attacks second
						attack(getEnMove(enemy),enemy,ally,sc); //performs an attack on ally
						attack(sel,ally,enemy,sc);
					}
					//applies turn end
					for(Pokemon p: userPkmn) p.endTurn();
					enemy.endTurn();
				}
			} else if(comm.equals("retreat")){
				//ally retreats
				int newAlly = retreat(ally,enemy,userPkmn,sc);
				if(newAlly != -1){
					ally = userPkmn.get(newAlly); //sets ally to new ally
				}
			} else if(comm.equals("pass")){
				//ally passes
				pass(ally,enemy,userPkmn,sc);
			}
			
			//checks for deaths (I mean faints)
			if(enemy.getHp() == 0){
				//enemy faints
				otherPkmn.remove(enemy); //removes the enemy
				System.out.println(enemy.getName() + " has fainted! " + otherPkmn.size() + " remain!");
				if(otherPkmn.size() == 0){
					//you win when all enemy pokemon are dead
					System.out.println("You have defeated all Pokemon and are now the ultimate Pokemon trainer!");
					running = false;
					break;
				}
				for(Pokemon p: userPkmn) p.endBattle(); //heals all pokemon alive
				enemy = getRandomPkmn(otherPkmn,sc); //resets enemy if they've been defeated
				ally = getAllyPokemon(userPkmn,sc); //allows user to repick their pokemon to fight the new enemy
			}
			if(ally.getHp() == 0){
				//ally faints
				userPkmn.remove(ally); //removes dead ally
				System.out.println(ally.getName() + " has fainted!");
				makeUserEnter();
				if(userPkmn.size() == 0){
					System.out.println("You have no pokemon left! You lose!");
					running = false;
					break;
				}
				//if not all our pokemon are dead we request a new one
				ally = getAllyPokemon(userPkmn,sc); //requests a new pokemon
			}
		}
		
		//HANDLES PLAY AGAIN
		if (confirm("Play again? [Y/N]",sc)) //asks if user wants to play again
			main(args);//re-runs the program
	}
}