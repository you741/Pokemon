//Pokemon.java

class Pokemon{
	//CONSTANTS
	public static final int SUPER_EFFECTIVE = 1;
	public static final int NOT_EFFECTIVE = 0;
	public static final int NORMAL_DAMAGE = -1;
	
	//stats
	private String name; //pkmn name
	private int hp,currHp; //pkmn max hp and current hp
	private String type; //type
	private String res; //resistance
	private String weak; //weakness
	private int numAtks; //number of attacks/moves
	private Move[] moves; //all attacks/moves pokemon has
	private int energy; //energy a pokemon has
	
	//special effects
	private int stunned; //how long pokemon is stunned for
	private boolean disabled; //is the pokemon disabled?
	
	//fields for results of last attack
	private boolean missed; //if attack missed
	private int effectiveness; //effectiveness (super, not very or nothing)
	private int timesHit; //times hit - only for wild storm
	
	public Pokemon(String[] stats){
		//initializes pokemon
		this.name = stats[0];
		this.hp = Integer.parseInt(stats[1]);
		this.currHp = hp;
		this.type = stats[2];
		this.res = stats[3]; //resistance
		this.weak = stats[4]; //weakness
		this.numAtks = Integer.parseInt(stats[5]); //number of attacks
		this.moves = new Move[numAtks];
		for(int i = 0;i < numAtks;i++){
			//i is current move
			String moveName = stats[6+i*4];
			int energyCost = Integer.parseInt(stats[6+i*4+1]);
			int damage = Integer.parseInt(stats[6+i*4+2]);
			String special;
			if(6 + i*4 + 3 < stats.length) //makes sure the special string is within the boundaries
				special = stats[6+i*4+3];
			else{
				special = " ";
			}
			moves[i] = new Move(moveName,energyCost,damage,special); //sets move to (name,energy_cost,damage,special)
		}
		this.energy = 50;
		this.stunned = 0;
		this.disabled = false;
		this.missed = false;
		this.effectiveness = NORMAL_DAMAGE;
		this.timesHit = 0;
	}
	
	//GAMEPLAY METHODS
	public boolean canUseAttack(int moveInd){
		//can the Pokemon use the attack?
		if(moves[moveInd].energy > this.energy){
			return false; //can't use the attack
		}
		return true;
	}
	public int attack(int moveInd, Pokemon enemy){
		//makes an attack on a pokemon - returns damage
		Move move = moves[moveInd]; //move pokemon is using
		int dmg = move.damage; //damage dealt - can be modified
		if(energy < move.energy) return 0; //if we don't have enough energy we don't attack
		setEnergy(energy - move.energy); //removes energy cost from Pokemon's energy
		
		//CALCULATES RESISTANCES AND WEAKNESSES
		effectiveness = NORMAL_DAMAGE; //default is normal damage
		if(enemy.weak.equals(this.type)){
			dmg *= 2; //doubles if the enemy is weak to this pokemon
			effectiveness = SUPER_EFFECTIVE;
		} else if(enemy.res.equals(this.type)){
			dmg /= 2; //halves if the enemy is resistant to this pokemon
			effectiveness = NOT_EFFECTIVE;
		}
		
		//DISABLED MODIFIER
		if(this.disabled){
			dmg -= 10; //10 less damage if disabled
			if(dmg < 0)
				dmg = 0; //prevents negative damage
		}
		//SPECIAL EFFECTS
		String special = move.special; //special effect
		missed = false; //default is not missed
		timesHit = 0; //default is hit 0 times
		if(special.equals("stun")){
			//50% chance stun for 1 turn (after this one)
			if(Math.random() >= 0.5)
				enemy.stunned = 2;
		} else if(special.equals("wild card")){
			//50% chance damage is not done
			if(Math.random() >= 0.5){
				dmg = 0;
				missed = true;
			}
		} else if(special.equals("wild storm")){
			//50% chance of hitting and infinite loop
			int oDmg = dmg; //original dmg - dmg increases by this amount every time we hit
			dmg = 0; //dmg starts off at 0 as we may have missed
			while(true){
				if(Math.random() >= 0.5){
					dmg += oDmg; //increases dmg
					timesHit++;
				} else{
					if(timesHit == 0) missed = true; //if we never hit once we missed
					break;
				}
			}
		} else if(special.equals("disable")){
			//disables enemy
			enemy.disabled = true;
		} else if(special.equals("recharge")){
			//gains 20 energy
			setEnergy(energy+20);
		}
		enemy.setHp(enemy.currHp - dmg); //sets the hp to a value dmg less (does no damage if wild storm)
		return dmg;
	}
	public void turnEnd(){
		//does the necessary stuff when a pokemon ends its turn
		stunned--; //reduces stun time
		if (stunned < 0)
			stunned = 0; //can't have negative stun time
		setEnergy(energy + 10); //increases energy by 10
	}
	public void battleEnd(){
		//does stuff necessary at the end of a battle
		stunned = 0; //not stunned
		disabled = false; //sets disabled to false
		setHp(currHp+20); //heals to 20+
		setEnergy(50); //sets energy to 50
	}
	
	//SET METHODS
	public void setHp(int hp){
		//sets the current hp to the specified value
		if (hp < 0){
			hp = 0; //prevents negative hp values
		}
		if (hp > this.hp){
			hp = this.hp; //prevents from going above maxHp
		}
		this.currHp = hp;
	}
	public void setEnergy(int energy){
		//set energy to specified energy
		if(energy < 0){
			energy = 0; //prevents negative energy
		}
		if(energy > 50){
			energy = 50; //prevents energy from exceeding 50
		}
		this.energy = energy;
	}
	
	//GET METHODS
	public String getName(){
		return name;
	}
	public int getHp(){
		//gets current hp
		return currHp;
	}
	public int getMaxHp(){
		//returns maximum hp
		return hp;
	}
	public int getEnergy(){
		//returns the energy
		return energy;
	}
	public String getType(){
		//returns the type
		return type;
	}
	public int getNumMoves(){
		//returns number of attacks
		return numAtks;
	}
	public boolean getStunned(){
		//returns if pokemon is stunned or not
		return stunned > 0;
	}
	public boolean getDisabled(){
		//returns if pokemon is disabled or not
		return disabled;
	}
	public int getTimesHit(){
		//returns time hit in last battle
		return timesHit;
	}
	public int getEffectiveness(){
		//returns effectiveness of last attack
		return effectiveness;
	}
	public boolean getMissed(){
		//returns if last hit missed
		return missed;
	}
	
	//GETTING MOVE INFO
	public String getMoveName(int i){
		//gets the name of the ith move
		return moves[i].name;
	}
	public String getMoveSpecial(int i){
		//gets the special of the ith move
		return moves[i].special;
	}
	//DISPLAY METHODS
	public String toString(){
		//String of pokemon's basic data
		String moves = getMovesAsString();
		return "Name: " + name + "\n" +
			"Hp: " + currHp + "/" + hp + "\n" +
			"Energy: " + energy + "/50\n" +
		 	"Type: " + type + "\n" +
		 	"Resistance: " + res + "\n" +
		 	"Weakness: " + weak + "\n" +
		 	"Number of Attacks: " + numAtks + "\n" +
		 	moves +
		 	(disabled?"Disabled\n":"") +
		 	((stunned>0)?"Stunned\n":"");
	}
	public String getMovesAsString(){
		//returns a String of the pkmn's attacks
		String movesAsString = "";
		for(int i = 0;i < numAtks;i++){
			movesAsString += "=========================\n" +
				"Move " + (i+1) + ": " + moves[i] +
				"=========================\n";
		}
		return movesAsString;
	}
	class Move{
		String name;
		int energy; //energy it costs
		int damage; //damage done
		String special; //special attack ability (STUN, WILD CARD, WILD STORM, RECHARGE)
		public Move(String name, int energy, int damage, String special){
			this.name = name;
			this.energy = energy;
			this.damage = damage;
			this.special = special;
		}
		public String toString(){
			return name + " \nDamage: " + damage + "\n" +
				"Energy cost: " + energy + "\n" +
				"Special: " + special + "\n";
		}
	}
}