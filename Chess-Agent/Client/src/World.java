import java.util.*;
import java.util.Map.Entry;


public class World
{
	private ArrayList<TranspositionTable> tablesList = new ArrayList<TranspositionTable>();
	private String[][] board = null;
	private int rows = 7;
	private int columns = 5;
	private int myColor = 0;
	private ArrayList<String> availableMoves = null;
	private int rookBlocks = 3;		// rook can move towards <rookBlocks> blocks in any vertical or horizontal direction
	private int nTurns = 0;
	private int nBranches = 0;
	private int noPrize = 9;
	
	private int bestmoveEval=0;
	private String bestmove=null;
	private String tupleMove=null;
	private int kingVal=8,rookVal=3,pawnVal=1,prizeVal=1;
	private int depth=7;
	private int whiteScore=0,blackScore=0;
	private int kingWeight=15,rookWeight=6,pawnWeight=2;
	
	
	public World()
	{
		board = new String[rows][columns];
		
		/* represent the board
		
		BP|BR|BK|BR|BP
		BP|BP|BP|BP|BP
		--|--|--|--|--
		P |P |P |P |P 
		--|--|--|--|--
		WP|WP|WP|WP|WP
		WP|WR|WK|WR|WP
		*/
		
		// initialization of the board
		for(int i=0; i<rows; i++)
			for(int j=0; j<columns; j++)
				board[i][j] = " ";
		
		// setting the black player's chess parts
		
		// black pawns
		for(int j=0; j<columns; j++)
			board[1][j] = "BP";
		
		board[0][0] = "BP";
		board[0][columns-1] = "BP";
		
		// black rooks
		board[0][1] = "BR";
		board[0][columns-2] = "BR";
		
		// black king
		board[0][columns/2] = "BK";
		
		// setting the white player's chess parts
		
		// white pawns
		for(int j=0; j<columns; j++)
			board[rows-2][j] = "WP";
		
		board[rows-1][0] = "WP";
		board[rows-1][columns-1] = "WP";
		
		// white rooks
		board[rows-1][1] = "WR";
		board[rows-1][columns-2] = "WR";
		
		// white king
		board[rows-1][columns/2] = "WK";
		
		// setting the prizes
		for(int j=0; j<columns; j++)
			board[rows/2][j] = "P";
		
		availableMoves = new ArrayList<String>();
	}
	
	public void setMyColor(int myColor)
	{
		this.myColor = myColor;
	}
	
	public String selectAction()
	{
		//long start = System.currentTimeMillis();

		String[][] tupleBoard = new String[rows][columns];
		for (int i=0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				tupleBoard[i][j]=board[i][j];	
			}
		}

		TranspositionTable tuple=new TranspositionTable(tupleBoard,tupleMove);

		
		availableMoves = new ArrayList<String>();
		if(myColor == 0) {		// I am the white player
			this.whiteMoves();			

		}else {					// I am the black player
			this.blackMoves();
	
		}
		// keeping track of the branch factor
		nTurns++;
		nBranches += availableMoves.size();
		
		String[][] temp_board=temp_board();//save the chessboard as it is before minimax
		
		int maxeval=Integer.MIN_VALUE;
		int alpha=Integer.MIN_VALUE;
		int beta=Integer.MAX_VALUE;
		ArrayList<String> sorted= new ArrayList<String>();
		whiteScore=Client.getScoreWhite();
		blackScore=Client.getScoreBlack();
		
		 
		sorted=sort(availableMoves);
		boolean same=false;
		for(TranspositionTable tryOut: tablesList) {
			for (int i=0;i<rows;i++) {
				for(int j=0;j<columns;j++) {
					if(board[i][j]!=tryOut.tableBoard[i][j]) {	
						same=true;
					}
				}
			}
			if(same==false) {
				return tryOut.tableMove;
			}
		}
		 
		for (String move : sorted) {
			int totalScore=0;
			if (myColor==0) {
				whiteScore+=calcScoreMinimax(move);
				totalScore=whiteScore-blackScore;
			}else {
				blackScore+=calcScoreMinimax(move);
				totalScore=blackScore-whiteScore;
			}
			if(calcScoreMinimax(move)==8 && totalScore>-8) {
				return move;
			}
			makeMoveMinimax(move);
			
			bestmoveEval=minimax(move,depth,false,alpha,beta,whiteScore,blackScore);
			if (bestmoveEval>maxeval) {
				maxeval=bestmoveEval;
				bestmove=move;
			}
			whiteScore=Client.getScoreWhite();
			blackScore=Client.getScoreBlack();

			
			for (int i=0;i<rows;i++) {
				for(int j=0;j<columns;j++) {
					board[i][j]=temp_board[i][j];	
				}
			}
		}
		tuple.tableMove=bestmove;
		tablesList.add(tuple);
		 // call to the methods you want to benchmark
//		long end = System.currentTimeMillis();
//		long result = end - start;
//		double resultSec = (double) result / 1000;
//		System.out.println("AB-PRUNING time: "+resultSec);
		return bestmove;
	}

	private void whiteMoves()
	{
		String firstLetter = "";
		String secondLetter = "";
		String move = "";
				
		for(int i=0; i<rows; i++)
		{
			for(int j=0; j<columns; j++)
			{
				firstLetter = Character.toString(board[i][j].charAt(0));
				
				// if it there is not a white chess part in this position then keep on searching
				if(firstLetter.equals("B") || firstLetter.equals(" ") || firstLetter.equals("P"))
					continue;
				
				// check the kind of the white chess part
				secondLetter = Character.toString(board[i][j].charAt(1));
				
				if(secondLetter.equals("P"))	// it is a pawn
				{
					
					// check if it can move one vertical position ahead
					firstLetter = Character.toString(board[i-1][j].charAt(0));
					
					if(firstLetter.equals(" ") || firstLetter.equals("P"))
					{
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i-1) + Integer.toString(j);
						
						availableMoves.add(move);
					}
					
					// check if it can move crosswise to the left
					if(j!=0 && i!=0)
					{
						firstLetter = Character.toString(board[i-1][j-1].charAt(0));						
						if(!(firstLetter.equals("W") || firstLetter.equals(" ") || firstLetter.equals("P"))) {
							move = Integer.toString(i) + Integer.toString(j) + 
									   Integer.toString(i-1) + Integer.toString(j-1);
								
							availableMoves.add(move);
						}											
					}
					
					// check if it can move crosswise to the right
					if(j!=columns-1 && i!=0)
					{
						firstLetter = Character.toString(board[i-1][j+1].charAt(0));
						if(!(firstLetter.equals("W") || firstLetter.equals(" ") || firstLetter.equals("P"))) {
							
							move = Integer.toString(i) + Integer.toString(j) + 
									   Integer.toString(i-1) + Integer.toString(j+1);							
							availableMoves.add(move);
						}
					}
				}
				else if(secondLetter.equals("R"))	// it is a rook
				{
					// check if it can move upwards
					for(int k=0; k<rookBlocks; k++)
					{
						if((i-(k+1)) < 0)
							break;
						
						firstLetter = Character.toString(board[i-(k+1)][j].charAt(0));
						
						if(firstLetter.equals("W"))
							break;
						
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i-(k+1)) + Integer.toString(j);
						
						availableMoves.add(move);
						
						// prevent detouring a chesspart to attack the other
						if(firstLetter.equals("B") || firstLetter.equals("P"))
							break;
					}
					
					// check if it can move downwards
					for(int k=0; k<rookBlocks; k++)
					{
						if((i+(k+1)) == rows)
							break;
						
						firstLetter = Character.toString(board[i+(k+1)][j].charAt(0));
						
						if(firstLetter.equals("W"))
							break;
						
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i+(k+1)) + Integer.toString(j);
						
						availableMoves.add(move);
						
						// prevent detouring a chesspart to attack the other
						if(firstLetter.equals("B") || firstLetter.equals("P"))
							break;
					}
					
					// check if it can move on the left
					for(int k=0; k<rookBlocks; k++)
					{
						if((j-(k+1)) < 0)
							break;
						
						firstLetter = Character.toString(board[i][j-(k+1)].charAt(0));
						
						if(firstLetter.equals("W"))
							break;
						
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i) + Integer.toString(j-(k+1));
						
						availableMoves.add(move);
						
						// prevent detouring a chesspart to attack the other
						if(firstLetter.equals("B") || firstLetter.equals("P"))
							break;
					}
					
					// check of it can move on the right
					for(int k=0; k<rookBlocks; k++)
					{
						if((j+(k+1)) == columns)
							break;
						
						firstLetter = Character.toString(board[i][j+(k+1)].charAt(0));
						
						if(firstLetter.equals("W"))
							break;
						
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i) + Integer.toString(j+(k+1));
						
						availableMoves.add(move);
						
						// prevent detouring a chesspart to attack the other
						if(firstLetter.equals("B") || firstLetter.equals("P"))
							break;
					}
				}
				else // it is the king
				{
					// check if it can move upwards
					if((i-1) >= 0)
					{
						firstLetter = Character.toString(board[i-1][j].charAt(0));
						
						if(!firstLetter.equals("W"))
						{
							move = Integer.toString(i) + Integer.toString(j) + 
								   Integer.toString(i-1) + Integer.toString(j);
								
							availableMoves.add(move);	
						}
					}
					
					// check if it can move downwards
					if((i+1) < rows)
					{
						firstLetter = Character.toString(board[i+1][j].charAt(0));
						
						if(!firstLetter.equals("W"))
						{
							move = Integer.toString(i) + Integer.toString(j) + 
								   Integer.toString(i+1) + Integer.toString(j);
								
							availableMoves.add(move);	
						}
					}
					
					// check if it can move on the left
					if((j-1) >= 0)
					{
						firstLetter = Character.toString(board[i][j-1].charAt(0));
						
						if(!firstLetter.equals("W"))
						{
							move = Integer.toString(i) + Integer.toString(j) + 
								   Integer.toString(i) + Integer.toString(j-1);
								
							availableMoves.add(move);	
						}
					}
					
					// check if it can move on the right
					if((j+1) < columns)
					{
						firstLetter = Character.toString(board[i][j+1].charAt(0));
						
						if(!firstLetter.equals("W"))
						{
							move = Integer.toString(i) + Integer.toString(j) + 
								   Integer.toString(i) + Integer.toString(j+1);
								
							availableMoves.add(move);	
						}
					}
				}			
			}	
		}
	}
	
	private void blackMoves()
	{
		String firstLetter = "";
		String secondLetter = "";
		String move = "";
				
		for(int i=0; i<rows; i++)
		{
			for(int j=0; j<columns; j++)
			{
				firstLetter = Character.toString(board[i][j].charAt(0));
				
				// if it there is not a black chess part in this position then keep on searching
				if(firstLetter.equals("W") || firstLetter.equals(" ") || firstLetter.equals("P"))
					continue;
				
				// check the kind of the white chess part
				secondLetter = Character.toString(board[i][j].charAt(1));
				
				if(secondLetter.equals("P"))	// it is a pawn
				{
					
					// check if it can move one vertical position ahead
					firstLetter = Character.toString(board[i+1][j].charAt(0));
					
					if(firstLetter.equals(" ") || firstLetter.equals("P"))
					{
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i+1) + Integer.toString(j);
						
						availableMoves.add(move);
					}
					
					// check if it can move crosswise to the left
					if(j!=0 && i!=rows-1)
					{
						firstLetter = Character.toString(board[i+1][j-1].charAt(0));
						
						if(!(firstLetter.equals("B") || firstLetter.equals(" ") || firstLetter.equals("P"))) {
							move = Integer.toString(i) + Integer.toString(j) + 
									   Integer.toString(i+1) + Integer.toString(j-1);
								
							availableMoves.add(move);
						}																	
					}
					
					// check if it can move crosswise to the right
					if(j!=columns-1 && i!=rows-1)
					{
						firstLetter = Character.toString(board[i+1][j+1].charAt(0));
						
						if(!(firstLetter.equals("B") || firstLetter.equals(" ") || firstLetter.equals("P"))) {
							move = Integer.toString(i) + Integer.toString(j) + 
									   Integer.toString(i+1) + Integer.toString(j+1);
								
							availableMoves.add(move);
						}
							
						
						
					}
				}
				else if(secondLetter.equals("R"))	// it is a rook
				{
					// check if it can move upwards
					for(int k=0; k<rookBlocks; k++)
					{
						if((i-(k+1)) < 0)
							break;
						
						firstLetter = Character.toString(board[i-(k+1)][j].charAt(0));
						
						if(firstLetter.equals("B"))
							break;
						
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i-(k+1)) + Integer.toString(j);
						
						availableMoves.add(move);
						
						// prevent detouring a chesspart to attack the other
						if(firstLetter.equals("W") || firstLetter.equals("P"))
							break;
					}
					
					// check if it can move downwards
					for(int k=0; k<rookBlocks; k++)
					{
						if((i+(k+1)) == rows)
							break;
						
						firstLetter = Character.toString(board[i+(k+1)][j].charAt(0));
						
						if(firstLetter.equals("B"))
							break;
						
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i+(k+1)) + Integer.toString(j);
						
						availableMoves.add(move);
						
						// prevent detouring a chesspart to attack the other
						if(firstLetter.equals("W") || firstLetter.equals("P"))
							break;
					}
					
					// check if it can move on the left
					for(int k=0; k<rookBlocks; k++)
					{
						if((j-(k+1)) < 0)
							break;
						
						firstLetter = Character.toString(board[i][j-(k+1)].charAt(0));
						
						if(firstLetter.equals("B"))
							break;
						
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i) + Integer.toString(j-(k+1));
						
						availableMoves.add(move);
						
						// prevent detouring a chesspart to attack the other
						if(firstLetter.equals("W") || firstLetter.equals("P"))
							break;
					}
					
					// check of it can move on the right
					for(int k=0; k<rookBlocks; k++)
					{
						if((j+(k+1)) == columns)
							break;
						
						firstLetter = Character.toString(board[i][j+(k+1)].charAt(0));
						
						if(firstLetter.equals("B"))
							break;
						
						move = Integer.toString(i) + Integer.toString(j) + 
							   Integer.toString(i) + Integer.toString(j+(k+1));
						
						availableMoves.add(move);
						
						// prevent detouring a chesspart to attack the other
						if(firstLetter.equals("W") || firstLetter.equals("P"))
							break;
					}
				}
				else // it is the king
				{
					// check if it can move upwards
					if((i-1) >= 0)
					{
						firstLetter = Character.toString(board[i-1][j].charAt(0));
						
						if(!firstLetter.equals("B"))
						{
							move = Integer.toString(i) + Integer.toString(j) + 
								   Integer.toString(i-1) + Integer.toString(j);
								
							availableMoves.add(move);	
						}
					}
					
					// check if it can move downwards
					if((i+1) < rows)
					{
						firstLetter = Character.toString(board[i+1][j].charAt(0));
						
						if(!firstLetter.equals("B"))
						{
							move = Integer.toString(i) + Integer.toString(j) + 
								   Integer.toString(i+1) + Integer.toString(j);
								
							availableMoves.add(move);	
						}
					}
					
					// check if it can move on the left
					if((j-1) >= 0)
					{
						firstLetter = Character.toString(board[i][j-1].charAt(0));
						
						if(!firstLetter.equals("B"))
						{
							move = Integer.toString(i) + Integer.toString(j) + 
								   Integer.toString(i) + Integer.toString(j-1);
								
							availableMoves.add(move);	
						}
					}
					
					// check if it can move on the right
					if((j+1) < columns)
					{
						firstLetter = Character.toString(board[i][j+1].charAt(0));
						
						if(!firstLetter.equals("B"))
						{
							move = Integer.toString(i) + Integer.toString(j) + 
								   Integer.toString(i) + Integer.toString(j+1);
								
							availableMoves.add(move);	
						}
					}
				}			
			}	
		}
	}
	
	/*private String selectRandomAction()
	{		
		Random ran = new Random();
		int x = ran.nextInt(availableMoves.size());
		
		return availableMoves.get(x);
	}*/
	
	private int minimax(String move, int depth, boolean maximizer,int alpha,int beta, int whiteScore,int blackScore) {
		availableMoves = new ArrayList<String>();
		ArrayList<String> sorted= new ArrayList<String>();
		 
		if (depth==0 || terminal()) {

			return evaluation(whiteScore,blackScore);
		}
		String[][] temp_minimax_board=temp_board();
		int curWhiteScore=whiteScore;
		int curBlackScore=blackScore;
		
		if(maximizer) {
			int maxEval=Integer.MIN_VALUE;
			availableMoves.clear();

			if (myColor==0) {
				this.whiteMoves();
			}else {
				this.blackMoves();
			}
			sorted=sort(availableMoves);
			
			for (String maximove :sorted) {

				if (myColor==0) {
					whiteScore+=calcScoreMinimax(maximove);
				}else {
					blackScore+=calcScoreMinimax(maximove);
					
				}
				
				makeMoveMinimax(maximove);

				int eval=minimax(maximove,depth-1,false,alpha,beta,whiteScore,blackScore);
				maxEval=Math.max(eval, maxEval);

				
				for (int i=0;i<rows;i++) {
					for(int j=0;j<columns;j++) {
						board[i][j]=temp_minimax_board[i][j];	
					}
				}
				
				whiteScore=curWhiteScore;
				blackScore=curBlackScore;
				
				alpha=Math.max(eval, alpha);
				if (beta<=alpha) {
					break;
				}
			}

			return maxEval;
		}else {
			int minEval=Integer.MAX_VALUE;
			availableMoves.clear();

			if (myColor!=0) {
				this.whiteMoves();
			}else {
				this.blackMoves();
			}
			sorted=sort(availableMoves);
			for (String minimove : sorted) {
				if (myColor!=0) {
					whiteScore+=calcScoreMinimax(minimove);
				}else {
					blackScore+=calcScoreMinimax(minimove);
				}
				
				makeMoveMinimax(minimove);
				int eval=minimax(minimove,depth-1,true,alpha,beta,whiteScore,blackScore);
				minEval=Math.min(eval, minEval);
				
				for (int i=0;i<rows;i++) {
					for(int j=0;j<columns;j++) {
						board[i][j]=temp_minimax_board[i][j];	
					}
				}
				whiteScore=curWhiteScore;
				blackScore=curBlackScore;
				
			
				
				beta=Math.min(eval,beta);
				if(beta<=alpha) {
					break;
				}
			}
			return minEval;
		}
		
	}
	
	private int evaluation(int whiteScore,int blackScore) {
		int whiteKing=0,whiteRook=0,whitePawn=0,blackKing=0,blackRook=0,blackPawn=0;
		int materialBlack=0,materialWhite=0;
		
		for (int i=0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				if (Character.toString(board[i][j].charAt(0)).equals("B")) {
					if(board[i][j].contains("BK")){
						blackKing++;
						
					}else if (board[i][j].contains("BR")){
						blackRook++;
						
					}else if (board[i][j].contains("BP")) {
						blackPawn++;
					}
						
					}else {
						if(board[i][j].contains("WK")){
							whiteKing++;
							
						}else if (board[i][j].contains("WR")){
							whiteRook++;
							
						}else if (board[i][j].contains("WP")) {
							whitePawn++;
						}
						
					}
				}
			}
		
		materialBlack+=blackKing*kingWeight;
		materialBlack+=blackRook*rookWeight;
		materialBlack+=blackPawn*pawnWeight;
		
		materialWhite+=whiteKing*kingWeight;
		materialWhite+=whiteRook*rookWeight;
		materialWhite+=whitePawn*pawnWeight;
		
		int materialDif=(materialWhite+whiteScore)-(materialBlack+blackScore);
		
		int perspective=0;
		if (myColor==0) {
			perspective=1;
		}else {
			perspective=-1;
		}
		
		return materialDif*perspective;
		
		
	}
	
	private String[][] temp_board() {
		
		String[][] temp_board = new String[rows][columns];
		for (int i=0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				temp_board[i][j]=this.board[i][j];	
			}
		}
		return temp_board;
	}
	
	private ArrayList<String> sort(ArrayList<String> availableMoves){
		ArrayList<String> sortedMoves=new ArrayList<String>();
		TreeMap<String,Integer> map=new  TreeMap<String,Integer>();
		TreeMap<String,Integer> sortedMap=new  TreeMap<String,Integer>();

		int eval=-1;
		for (String move:availableMoves) {
			
			eval=calcScoreMinimax(move);				
			
			map.put(move, eval);
	
		}

		
		sortedMap = sortByValues(map);
		
		
		
	    for (String i : sortedMap.keySet()) {
	    	sortedMoves.add(i);
	    	
	    }

		ArrayList<String> sortedMovesFinal=new ArrayList<String>();

	    for(int i=sortedMoves.size()-1;i>=0;i--) {
	    	
            sortedMovesFinal.add(sortedMoves.get(i));
	    	
	    }
	    
	    
		return sortedMovesFinal;
	}
	

	 public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValues(final Map<K, V> map) {
	    Comparator<K> valueComparator = new Comparator<K>() {
	      public int compare(K k1, K k2) {
	        int compare = map.get(k1).compareTo(map.get(k2));
	        if (compare == 0) 
	          return 1;
	        else 
	          return compare;
	      }
	    };
	 
	    TreeMap<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	    }
	
	
	private int calcScoreMinimax (String move) {
		int whiteS=0,blackS=0;
		int from_x = Integer.parseInt(Character.toString(move.charAt(0)));
		int from_y = Integer.parseInt(Character.toString(move.charAt(1)));
		int to_x = Integer.parseInt(Character.toString(move.charAt(2)));
		int to_y = Integer.parseInt(Character.toString(move.charAt(3)));
		
		
		if(Character.toString(board[from_x][from_y].charAt(0)).equals("W")) {		// I am the white player
			if(Character.toString(board[to_x][to_y].charAt(0)).equals("B")) {//if there is black in the next position
				if(Character.toString(board[to_x][to_y].charAt(1)).equals("K")){//if it is king
					whiteS+=kingVal;
				}else if (Character.toString(board[to_x][to_y].charAt(1)).equals("R")) {//if it is rook
					whiteS+=rookVal;
				}else if(Character.toString(board[to_x][to_y].charAt(1)).equals("P")) {//if its is pawn
					whiteS+=pawnVal;
				}
				
			}else if (Character.toString(board[to_x][to_y].charAt(0)).equals("P")) {//if it is prize
				Random rand = new Random();
				if(rand.nextFloat()<=0.95) {
					whiteS+=prizeVal;	
				}
			}else if(Character.toString(board[from_x][from_y].charAt(1)).equals("P") && (from_x==1 && to_x==0) ) {//last row
				whiteS+=1;
				
			}
			
			return whiteS;
		}else {				// I am the black player 
			if(Character.toString(board[to_x][to_y].charAt(0)).equals("W")) {//if there is black in the next position
				if(Character.toString(board[to_x][to_y].charAt(1)).equals("K")){//if it is king
					blackS+=kingVal;
				}else if (Character.toString(board[to_x][to_y].charAt(1)).equals("R")) {//if it is rook
					blackS+=rookVal;
				}else if(Character.toString(board[to_x][to_y].charAt(1)).equals("P")) {//if its is pawn
					blackS+=pawnVal;
				}
				
			}else if (Character.toString(board[to_x][to_y].charAt(0)).equals("P")) {//if it is prize
				Random rand = new Random();
				if(rand.nextFloat()<=0.95) {
					blackS+=prizeVal;	
				}
			}else if(Character.toString(board[from_x][from_y].charAt(1)).equals("P") && (from_x==rows-2 && to_x==rows-1) ) {//last row
				blackS+=1;
				
			}
			return blackS;
		}
	}
	
	
	private boolean terminal () {
		
		boolean blackKing=false, whiteKing=false;
		int counter=0;
		for (int i=0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				if(board[i][j].contains("BK")) {//black king is in the game
					blackKing=true;
				}
				if(board[i][j].contains("WK")) {//white king is in the game
					whiteKing=true;
				}
				if (board[i][j].contains("BP") || board[i][j].contains("WP") ||board[i][j].contains("BR") || board[i][j].contains("WR") ) {//any rooks or pawns are in the game
					counter++;
				}
			}
		}
		
		if ((blackKing && !whiteKing) || (!blackKing && whiteKing)) {
			return true;
		}else if(counter==0 && whiteKing && blackKing) {
			return true;
		}
		
		return false;
		
		
	}
	
	private void makeMoveMinimax(String move) {
		
		int x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		int y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		int x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		int y2 = Integer.parseInt(Character.toString(move.charAt(3)));
		
		String chesspart = Character.toString(board[x1][y1].charAt(1));
		
		boolean pawnLastRow = false;
		
		// check if it is a move that has made a move to the last line
		if(chesspart.equals("P"))
			if( (x1==rows-2 && x2==rows-1) || (x1==1 && x2==0) )
			{
				board[x2][y2] = " ";	// in a case an opponent's chess part has just been captured
				board[x1][y1] = " ";
				pawnLastRow = true;
			}
		
		// otherwise
		if(!pawnLastRow)
		{
			board[x2][y2] = board[x1][y1];
			board[x1][y1] = " ";
		}
		
	}
	
	public double getAvgBFactor()
	{
		return nBranches / (double) nTurns;
	}
	
	public void makeMove(int x1, int y1, int x2, int y2, int prizeX, int prizeY)
	{
		String chesspart = Character.toString(board[x1][y1].charAt(1));
		
		boolean pawnLastRow = false;
		
		// check if it is a move that has made a move to the last line
		if(chesspart.equals("P"))
			if( (x1==rows-2 && x2==rows-1) || (x1==1 && x2==0) )
			{
				board[x2][y2] = " ";	// in a case an opponent's chess part has just been captured
				board[x1][y1] = " ";
				pawnLastRow = true;
			}
		
		// otherwise
		if(!pawnLastRow)
		{
			board[x2][y2] = board[x1][y1];
			board[x1][y1] = " ";
		}
		
		// check if a prize has been added in the game
		if(prizeX != noPrize)
			board[prizeX][prizeY] = "P";
	}
	
	
	
}
