//PokemonArena.java
//Pokemon Arena game that lets you fight pokemon
import java.io.*;
import java.util.*;

public class PokemonArena{
	//DATA MANAGEMENT METHODS
	public static boolean isNumeric(String str){
		//checks if something is an +integer
		return str.matches("\\d+");
	}
	public static Pokemon[] loadPokemon() throws IOException{
		//loads pokemon
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
	public static void getPokemonSelection(ArrayList<Pokemon> userPkmn, ArrayList<Pokemon> otherPkmn, Pokemon[] allPkmn, Scanner sc){
		while(userPkmn.size() < 4){
			System.out.print("Select a pokemon [Enter the number next to it's name] [info to ask for info]: ");
			String line = sc.nextLine(); //line entered
			if(line.toLowerCase().equals("info")){
				reqInfo(allPkmn,sc); //handles info query
				System.out.println("[Enter to continue]");
				sc.nextLine();
				printPokemon(allPkmn); //reprints all Pokemon
			} else if(line.toLowerCase().equals("quit")){
				System.out.println("Good bye! SAYONARA!");
				System.exit(0);
				break;
			} else{
				if(!isNumeric(line)){
					System.out.println("Enter an integer please");
					continue; //if not numeric we continue
				}
				int selected = Integer.parseInt(line);
				if(selected > 0 && selected <= allPkmn.length){
					if(userPkmn.contains(allPkmn[selected-1])){
						//if we already selected that pokemon we can't select it again so we continue
						System.out.println("You already selected that Pokemon");
						continue;
					}
					//if within the boundaries we add that pokemon to list and remove from otherPkmn
					userPkmn.add(allPkmn[selected-1]);
					otherPkmn.remove(allPkmn[selected-1]);
					System.out.println(allPkmn[selected-1].getName() + " added.");
				} else{
					System.out.println("Out of bounds. Enter a number between 1 and " + allPkmn.length);
				}
			}
		}
	}
	
	public static void reqInfo(ArrayList<Pokemon> allPkmn, Scanner sc){
		//requests info from user about a pokemon
		while(true){
			printPokemon(allPkmn); //prints all pokemon
			System.out.println("Enter a pokemon's number to get info about it:");
			String inp = sc.nextLine(); //user's input
			if(inp.toLowerCase().equals("quit")){
				System.out.println("Query cancelled.");
				break; //we stop querying if user enters quit
			}
			if(!isNumeric(inp)){
				System.out.println("Enter an integer please");
				System.out.println("[Enter to continue]");
				sc.nextLine(); //makes user press something before continuing
				continue;
			}
			int selected = Integer.parseInt(inp); //integer form of user's input
			if(selected >= 1 && selected <= allPkmn.size()){
				selected--; //brings selected to range of 0 - allPkmn.length-1
				System.out.println(allPkmn.get(selected)); //prints pokemon info
				break;
			} else{
				//out of bounds
				System.out.println("Out of bounds. Please choose a number between 1 and " + allPkmn.size());
				System.out.println("[Enter to continue]");
				sc.nextLine(); //makes user press enter before continuing
			}
		}
	}

	public static void reqInfo(Pokemon[] allPkmn, Scanner sc){
		//override with primitive array
		reqInfo(new ArrayList<Pokemon>(Arrays.asList(allPkmn)),sc);
	}

	public static void attack(int sel, Pokemon atker, Pokemon target, Scanner sc){
		//attacks with atker to target with selected move (sel)
		if(sel == -1 && atker.getHp() > 0){
			//For enemies only, means no available moves to use so they pass
			System.out.println(atker.getName() + " passes.");
		} else if(atker.getStunned() && atker.getHp() > 0){
			System.out.println(atker.getName() + " is stunned!"); //we don't attack if atker is stunned
		} else if(atker.getHp() > 0){
			//if atker is alive we attack with them
			System.out.printf("%s used %s on %s!\n",atker.getName(),atker.getMoveName(sel),target.getName());
			//previous statuses so I can print more precisely what the move did
			boolean wasDisabled = target.getDisabled(); //was it disabled
			
			if(atker.getMoveSpecial(sel).equals("recharge")){
				//displays recharge
				System.out.println(atker.getName() + " recharged some energy!");
			}
			int dmg = atker.attack(sel,target); //damage done
			if(!wasDisabled && target.getDisabled()){
				//if it was not disabled before and it is now we display that it was disabled
				System.out.println(target.getName() + " was disabled by the attack!");
			}
			if(atker.getMissed()){
				System.out.println(atker.getName() + " missed!");
			} else if(atker.getTimesHit() > 0){
				//if we didn't miss and we have more times hit then we had a wild storm attack
				System.out.println("Hit " + atker.getTimesHit() + " time(s)!"); //prints time hit
			}
			if(!atker.getMissed()){
				//if ally did not miss we print damage
				if(atker.getEffectiveness() == Pokemon.SUPER_EFFECTIVE) System.out.println("It's super effective!");
				if(atker.getEffectiveness() == Pokemon.NOT_EFFECTIVE) System.out.println("It's not very effective...");
				System.out.println(target.getName() + " took " + dmg + " damage!");
			}
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
			if(enUsableMoves.size() == 0){
				//enemy passes
				return -1;
			}
		}
		return enUsableMoves.get((int)(Math.random()*enUsableMoves.size())); //returns index of random move
	}
	
	public static Pokemon getRandomPkmn(ArrayList<Pokemon> pkmn, Scanner sc){
		//gets a random enemy to fight
		Pokemon enemy = pkmn.get((int)(Math.random()*pkmn.size())); //gets a random pkmn
		System.out.println(enemy.getName() + " is your opponent!");
		System.out.println("[Enter to continue]");
		sc.nextLine(); //just a placeholder so user has to enter before continuing
		System.out.println(enemy); //prints enemy data
		System.out.println("[Enter to continue]");
		sc.nextLine(); //just a placeholder so user has to enter before continuing
		return enemy;
	}
	
	public static Pokemon getAllyPokemon(ArrayList<Pokemon> userPkmn, Scanner sc){
		System.out.println("Select an ally: ");
		printPokemon(userPkmn);
		while(true){
			System.out.println("Choose an awake pokemon [info for info]: ");
			String line = sc.nextLine();
			if(line.toLowerCase().equals("info")){
				reqInfo(userPkmn,sc);
			}
			if(!isNumeric(line)){
				System.out.println("Enter an integer please"); //makes sure line is an integer
				continue;
			}
			int selected = Integer.parseInt(line);
			selected--; //brings index to the right scale (0 - 3)
			if(selected >= 0 && selected < userPkmn.size()){
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
			} else{
					System.out.println("Out of bounds");
			}
		}
	}
	//PRINT METHODS
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
			System.out.printf("%d. %-12s %-8s HP: %-3d/%-3d\n",i+1,pkmn.get(i).getName(),pkmn.get(i).getType(),pkmn.get(i).getHp(),pkmn.get(i).getMaxHp());
		}
	}
	public static void main(String[] args) throws IOException{
		Scanner sc; //scanners sc = io stream
		sc = new Scanner(System.in);
		//LOADS ALL POKEMON
		Pokemon[] allPkmn = loadPokemon(); //the pkmn
		ArrayList<Pokemon> userPkmn = new ArrayList<Pokemon>(); //user's pokemon
		ArrayList<Pokemon> otherPkmn = new ArrayList<Pokemon>(Arrays.asList(allPkmn)); //enemy pokemon (removes ally pokemon later)
		
		boolean running = true;
		//GAME START
		System.out.println("POKEMON ARENA BY YOU ZHOU");
		printPokemon(allPkmn); //prints the name of all Pokemon
		//POKEMON SELECTION
		getPokemonSelection(userPkmn,otherPkmn,allPkmn,sc); //gets pokemon selections from user
		
		//MAIN GAME LOOP
		if(running){
			System.out.println("Game starting."); //game start text
		} else{
			System.exit(0);
		}
		Pokemon enemy = getRandomPkmn(otherPkmn,sc); //gets a random enemy from otherPkmn
		Pokemon ally = getAllyPokemon(userPkmn,sc); //the ally pokemon on the field at the moment
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
				System.out.println("[Enter to continue]");
				sc.nextLine(); //makes user press something before continuing
			} else if(comm.equals("info")){
				reqInfo(allPkmn,sc);
				System.out.println("[Enter to continue]");
				sc.nextLine(); //makes user press something before continuing
			} else if(comm.equals("info a")){
				reqInfo(userPkmn,sc);
				System.out.println("[Enter to continue]");
				sc.nextLine();
			} else if(comm.equals("info e")){
				reqInfo(otherPkmn,sc);
				System.out.println("[Enter to continue]");
				sc.nextLine();
			} else if(comm.equals("attack")){
				if(ally.getStunned()){
					System.out.println(ally.getName() + " is stunned!"); //can't attack if we're stunned
					continue;
				}
				System.out.println(ally.getMovesAsString()); //prints ally's moves
				boolean cancelled = false; //makes sure user didn't cancel attack
				while(true){
					//requests a move from user
					System.out.print("Select a move [Enter move number, quit to cancel]: ");
					String line = sc.nextLine(); //user's input
					if(line.equals("quit")){
						cancelled = true;
						System.out.println("Attack cancelled");
						break;
					}
					if(!isNumeric(line)){
						//makes sure user enters an integer
						System.out.println("Please enter an integer");
						continue;
					}
					int sel = Integer.parseInt(line); //user's selection
					if(sel >= 1 && sel <= ally.getNumMoves()){
						//if the user's selection is in the range
						sel--;
						if(!ally.canUseAttack(sel)){
							System.out.println("Not enough energy to use that move");
							continue;
						}
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
						break;
					} else{
						System.out.println("No move exists with index " + sel);
					}
				}
				if(!cancelled){
					//applies turn end
					for(int i = 0;i < userPkmn.size();i++) userPkmn.get(i).turnEnd();
					enemy.turnEnd();
				}
			} else if(comm.equals("retreat")){
				ally = getAllyPokemon(userPkmn,sc); //gets a new ally
				attack(getEnMove(enemy),enemy,ally,sc); //performs an attack on ally
				for(int i = 0;i < userPkmn.size();i++) userPkmn.get(i).turnEnd(); //applies turn end
				enemy.turnEnd();
				continue;
			} else if(comm.equals("pass")){
				//ally passes
				System.out.println(ally.getName() + " passes.");
				attack(getEnMove(enemy),enemy,ally,sc); //performs an attack on ally
				for(int i = 0;i < userPkmn.size();i++) userPkmn.get(i).turnEnd(); //applies turn end
				enemy.turnEnd();
			}
			if(enemy.getHp() == 0){
				//enemy faints
				otherPkmn.remove(enemy); //removes the enemy
				System.out.println(enemy.getName() + " has fainted! " + otherPkmn.size() + " remain!");
				for(int i = 0;i < userPkmn.size();i++){
					//battle end
					userPkmn.get(i).battleEnd();
				}
				if(otherPkmn.size() == 0){
					System.out.println("You have defeated all Pokemon and are now the ultimate Pokemon trainer!");
					running = false;
					break;
				}
				enemy = getRandomPkmn(otherPkmn,sc); //resets enemy if they've been defeated
				ally = getAllyPokemon(userPkmn,sc);
				continue; //continues
			}
			if(ally.getHp() == 0){
				//ally faints
				userPkmn.remove(ally);
				System.out.println(ally.getName() + " has fainted!");
				System.out.println("[Enter to continue]");
				sc.nextLine();
				if(userPkmn.size() == 0){
					System.out.println("You have no pokemon left! You lose!");
					running = false;
					break;
				}
				ally = getAllyPokemon(userPkmn,sc);
			}
		}
		System.out.println("Play again? [Y/N]");
		String option = sc.nextLine();
		if(option.toUpperCase().equals("Y"))
			main(args);//re-runs the program
	}
}