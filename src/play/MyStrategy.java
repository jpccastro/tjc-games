package play;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gametree.GameNode;
import gametree.GameNodeDoesNotExistException;
import play.exception.InvalidStrategyException;

/**********************************************************************************
 * This strategy implements a modified version of the well-known tit-for-tat.
 * The first move is random, and all subsequent ones simply mimic the opponent's
 * previous move.
 *
 **********************************************************************************/
public class MyStrategy extends Strategy {

	private static final String P1_COOPERATE = "1:1:Cooperate";
	private static final String P2_COOPERATE = "2:1:Cooperate";
	private static final String P1_DEFECT = "1:1:Defect";
	private static final String P2_DEFECT = "2:1:Defect";

	private int opponentDefectionsAsP1 = 0;
	private int opponentDefectionsAsP2 = 0;

	private int timesToDefectAsP1 = 0;
	private int timesToDefectAsP2 = 0;

	private int timesToCooperateAsP1 = 0;
	private int timesToCooperateAsP2 = 0;

	private List<GameNode> getReversePath(GameNode current) {
		try {
			GameNode n = current.getAncestor();
			List<GameNode> l =  getReversePath(n);
			l.add(current);
			return l;
		} catch (GameNodeDoesNotExistException e) {
			List<GameNode> l = new ArrayList<GameNode>();
			l.add(current);
			return l;
		}
	}

	//Player should be either 1 or 2
	private void defect(PlayStrategy myStrategy, int player) {
		if(player == 1) {
			myStrategy.put(P1_DEFECT, Double.valueOf(1));
			myStrategy.put(P1_COOPERATE, Double.valueOf(0));
			System.out.println("My move as P1: " + P1_DEFECT);
			return;
		}

		if(player == 2) {
			myStrategy.put(P2_DEFECT, Double.valueOf(1));
			myStrategy.put(P2_COOPERATE, Double.valueOf(0));
			System.out.println("My move as P2: " + P2_DEFECT);
			return;
		}

		return; //Should never happen
	}

	// Player should be either 1 or 2
	private void cooperate(PlayStrategy myStrategy, int player) {
		if (player == 1) {
			myStrategy.put(P1_DEFECT, Double.valueOf(0));
			myStrategy.put(P1_COOPERATE, Double.valueOf(1));
			System.out.println("My move as P1: " + P1_COOPERATE);
			return;
		}

		if (player == 2) {
			myStrategy.put(P2_DEFECT, Double.valueOf(0));
			myStrategy.put(P2_COOPERATE, Double.valueOf(1));
			System.out.println("My move as P2: " + P2_COOPERATE);
			return;
		}

		return; // Should never happen
	}

	private void computeStrategy(List<GameNode> listP1,
			List<GameNode> listP2,
			PlayStrategy myStrategy,
			SecureRandom random) throws GameNodeDoesNotExistException {

		Set<String> opponentMoves = new HashSet<String>();

		//When we played as Player1 we are going to check what were the moves
		//of our opponent as player2.
		for(GameNode n: listP1) {
			if(n.isNature() || n.isRoot()) continue;
			if(n.getAncestor().isPlayer2()) {
				opponentMoves.add(n.getLabel());
			}
		}

		//When we played as Player2 we are going to check what were the moves
		//of our opponent as player1.
		for(GameNode n: listP2) {
			if(n.isNature() || n.isRoot()) continue;
			if(n.getAncestor().isPlayer1()) {
				opponentMoves.add(n.getLabel());
			}
		}

		String opponentMoveAsP1 = null;
		String opponentMoveAsP2 = null;
		Iterator<String> opponentMovesIt = opponentMoves.iterator();

		while(opponentMovesIt.hasNext()) {
			String move = opponentMovesIt.next();
			if (move.charAt(0) == '1') {
				opponentMoveAsP1 = move;
			} else {
				opponentMoveAsP2 = move;
			}
		}

		System.out.println("Opponent move as P1: " + opponentMoveAsP1);
		System.out.println("Opponent move as P2: " + opponentMoveAsP2);

		System.out.printf("Opponent has defected %d times as P1\n", opponentDefectionsAsP1);
		System.out.printf("Opponent has defected %d times as P2\n", opponentDefectionsAsP2);

		//As player 1
		if (opponentMoveAsP2.equals(P2_DEFECT)) {
			opponentDefectionsAsP2++;
			if (timesToDefectAsP1 == 0) {
				if(timesToCooperateAsP1 > 0) {
					cooperate(myStrategy, 1);
					timesToCooperateAsP1--;
				} else {
					timesToDefectAsP1 = opponentDefectionsAsP2;
					timesToCooperateAsP1 = 2;

					defect(myStrategy, 1);
					timesToDefectAsP1--;
				}
			} else {
				defect(myStrategy, 1);
				timesToDefectAsP1--;
			}
		} else {
			if(timesToDefectAsP1 > 0) {
				defect(myStrategy, 1);
				timesToDefectAsP1--;
			} else {
				cooperate(myStrategy, 1);
				if (timesToCooperateAsP1 > 0)
					timesToCooperateAsP1--;
			}
		}

		// As player 2
		if (opponentMoveAsP1.equals(P1_DEFECT)) {
			opponentDefectionsAsP1++;
			if (timesToDefectAsP2 == 0) {
				if (timesToCooperateAsP2 > 0) {
					cooperate(myStrategy, 2);
					timesToCooperateAsP2--;
				} else {
					timesToDefectAsP2 = opponentDefectionsAsP1;
					timesToCooperateAsP2 = 2;

					defect(myStrategy, 2);
					timesToDefectAsP2--;
				}
			} else {
				defect(myStrategy, 2);
				timesToDefectAsP2--;
			}
		} else {
			if (timesToDefectAsP2 > 0) {
				defect(myStrategy, 2);
				timesToDefectAsP2--;
			} else {
				cooperate(myStrategy, 2);
				if (timesToCooperateAsP2 > 0)
					timesToCooperateAsP2--;
			}
		}


		//We now set our strategy to have a probability of 1.0 for the moves used
		//by our adversary in the previous round and zero for the remaining ones.
		Iterator<String> myMoves = myStrategy.keyIterator();
		// List<String> myMovesAsP1 = new ArrayList<>();
		// List<String> myMovesAsP2 = new ArrayList<>();

		// myMoves.forEachRemaining(move -> {
		// 	if(move.charAt(0) == '1') {
		// 		myMovesAsP1.add(move);
		// 	} else {
		// 		myMovesAsP2.add(move);
		// 	}
		// });

		// myMoves = myStrategy.keyIterator(); //reset iterator

		// System.out.println("My moves as P1");
		// myMovesAsP1.forEach(move -> {
		// 	System.out.println(move);
		// });

		// System.out.println("Moves as P2");
		// myMovesAsP2.forEach(move -> {
		// 	System.out.println(move);
		// });

		// while(myMoves.hasNext()) {
		// 	String k = myMoves.next();
		// 	if(opponentMoves.contains(k)) {
		// 		myStrategy.put(k, Double.valueOf(1));
		// 		System.err.println("Setting " + k + " to prob 1.0");
		// 	} else {
		// 		myStrategy.put(k, Double.valueOf(0));
		// 		System.err.println("Setting " + k + " to prob 0.0");
		// 	}
		// }


		//The following piece of code has the goal of checking if there was a portion
		//of the game for which we could not infer the moves of the adversary (because
		//none of the games in the previous round pass through those paths)
		Iterator<Integer> validationSetIte = tree.getValidationSet().iterator();
		myMoves = myStrategy.keyIterator();
		while(validationSetIte.hasNext()) {
			int possibleMoves = validationSetIte.next().intValue();
			String[] labels = new String[possibleMoves];
			double[] values = new double[possibleMoves];
			double sum = 0;
			for(int i = 0; i < possibleMoves; i++) {
				labels[i] = myMoves.next();
				values[i] = ((Double) myStrategy.get(labels[i])).doubleValue();
				sum += values[i];
			}
			if(sum != 1) { //In the previous game we could not infer what the adversary played here
				//Random move on this validation set
				sum = 0;
				for(int i = 0; i < values.length - 1; i++) {
					values[i] = random.nextDouble();
					while(sum + values[i] >= 1) values[i] = random.nextDouble();
					sum = sum + values[i];
				}
				values[values.length-1] = ((double) 1) - sum;

				for(int i = 0; i < possibleMoves; i++) {
					myStrategy.put(labels[i], values[i]);
					System.err.println("Unexplored path: Setting " + labels[i] + " to prob " + values[i]);
				}
			}

		}

	}


	@Override
	public void execute() throws InterruptedException {

		SecureRandom random = new SecureRandom();

		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}

		GameNode finalP1 = null;
		GameNode finalP2 = null;

		while(true) {

			PlayStrategy myStrategy = this.getStrategyRequest();
			if(myStrategy == null) //Game was terminated by an outside event
				break;
			boolean playComplete = false;

			while(! playComplete ) {
				if(myStrategy.getFinalP1Node() != -1) {
					finalP1 = this.tree.getNodeByIndex(myStrategy.getFinalP1Node());
					if(finalP1 != null)
						System.out.println("Terminal node in last round as P1: " + finalP1);
				}

				if(myStrategy.getFinalP2Node() != -1) {
					finalP2 = this.tree.getNodeByIndex(myStrategy.getFinalP2Node());
					if(finalP2 != null)
						System.out.println("Terminal node in last round as P2: " + finalP2);
				}

				Iterator<Integer> iterator = tree.getValidationSet().iterator();
				Iterator<String> keys = myStrategy.keyIterator();

				if(finalP1 == null || finalP2 == null) {
					//This is the first round so we always cooperate.
					cooperate(myStrategy, 1);
					cooperate(myStrategy, 2);

					// while(iterator.hasNext()) {
					// 	double[] moves = new double[iterator.next()];
					// 	double sum = 0;
					// 	for(int i = 0; i < moves.length - 1; i++) {
					// 		moves[i] = 1.0;
					// 		while(sum + moves[i] >= 1) moves[i] = random.nextDouble();
					// 		sum = sum + moves[i];
					// 	}
					// 	moves[moves.length-1] = ((double) 1) - sum;

					// 	for(int i = 0; i < moves.length; i++) {
					// 		if(!keys.hasNext()) {
					// 			System.err.println("PANIC: Strategy structure does not match the game.");
					// 			return;
					// 		}
					// 		myStrategy.put(keys.next(), moves[i]);
					// 	}
					// }
				} else {
					//Lets mimic our adversary strategy (at least what we can infer)
					List<GameNode> listP1 = getReversePath(finalP1);
					List<GameNode> listP2 = getReversePath(finalP2);

					try { computeStrategy(listP1, listP2, myStrategy, random); }
					catch( GameNodeDoesNotExistException e ) {
						System.err.println("PANIC: Strategy structure does not match the game.");
					}
				}

				try{
					this.provideStrategy(myStrategy);
					playComplete = true;
				} catch (InvalidStrategyException e) {
					System.err.println("Invalid strategy: " + e.getMessage());;
					e.printStackTrace(System.err);
				}
			}
		}

	}
}
