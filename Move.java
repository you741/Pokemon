//Move.java
//Class for the pokemon attacks

class Move{
	private String name;
	private int energy; //energy it costs
	private int damage; //damage done
	private String special; //special attack ability (STUN, WILD CARD, WILD STORM, RECHARGE)
	public Move(String name, int energy, int damage, String special){
		this.name = name;
		this.energy = energy;
		this.damage = damage;
		this.special = special;
	}
	//GET METHODS
	public String getName(){
		return name;
	}
	public int getEnergyCost(){
		return energy;
	}
	public int getDamage(){
		return damage;
	}
	public String getSpecial(){
		return special;
	}
}