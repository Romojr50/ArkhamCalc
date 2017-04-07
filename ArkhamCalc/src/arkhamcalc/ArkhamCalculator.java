package arkhamcalc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A class made to calculate probabilities related to games like Arkham Horror.
 * These games have the players roll a number of dice and try to get at least
 * some number of "successes." A success is usually a 5 or 6 on a 6-sided die.
 * Certain statuses can add or subtract sides of the die to the list of successes.
 */
public class ArkhamCalculator {

	private static JFrame frame;				//The UI frame
	
	private static JLabel numDiceLabel;			//Labels the input for dice
	private static JLabel numSuccessLabel;		//Labels the input for successes
	private static JLabel blessCurseLabel;		
			//Labels the radio buttons for statuses
	private static JLabel chanceLabel;			//Labels the chance output
	private static JLabel chancePercent;		//Displays the chance output
	
	private static SpinnerModel numDice;		//Spinner for number of dice
	private static JSpinner numDiceSpin;	
	private static SpinnerModel numSuccess;		//Spinner for number of successes
	private static JSpinner numSuccessSpin;
	private static JRadioButton blessButton;	//Button for blessed status
	private static JRadioButton normalButton;	//Button for normal status
	private static JRadioButton curseButton;	//Button for cursed status
	private static ButtonGroup statusGroup;		//Groups the radio buttons
	
	private static JButton calcButton;
			/*Unnecessary, but included for user peace of mind; if they click it
			   then the user knows the correct percentage is currently 
			   displayed*/
	
	/**
	 * The main method does almost nothing on its own. It calls the method to
	 * initialize the UI and then calls the calculation in order to initialize
	 * the output for the default values.
	 * @param args - None
	 */
	public static void main(String[] args) {
        initializeUI();
        performCalculation();
	}
	
	/**
	 * This method handles the calculation of the probability. It gathers the
	 * input first. Then it calls one of two helper methods. Finally, it displays
	 * the result.
	 */
	private static void performCalculation() {
		int dice = ((Integer) numDiceSpin.getValue()).intValue();
		int successNeeded = ((Integer) numSuccessSpin.getValue()).intValue();
		int status = getStatus();
		double result;
		
		/* Check for early exit condition. Then find out which helper method to
		 * use based on which will require the fewest calculations. */
		if (dice == 0 || successNeeded > dice) {
			result = 0.0;
		} else if (successNeeded > (dice / 2)) {
			result = calculateBySuccess(dice, successNeeded, status);
		} else {
			result = calculateByMiss(dice, successNeeded, status);
		}
		
		chancePercent.setText((result * 100) + "%");
	}
	
	/**
	 * This helper method calculates the probability by straight-up checking the
	 * chances of getting successes.
	 * For (successNeeded -> numberOfDice): calculate the chance of getting
	 *    exactly successNeeded successes on a roll and add it to a running
	 *    total.
	 * Then return the running total.
	 * Chance of getting exactly successNeeded successes:
	 *    (numberOfDice choose successNeeded) * 
	 *    (successChance)^(successNeeded) * 
	 *    (missChance)^(numberOfDice - successNeeded)
	 * @param dice - Number of total dice being rolled
	 * @param successNeeded - Number of successes needed
	 * @param status - Status (normal, blessed, or cursed)
	 * @return - Probability that the roll was successful.
	 */
	private static double calculateBySuccess(int dice, int successNeeded, 
			int status) {
		int curChoose = successNeeded;
		double hitChance = 1.0 / ((double)status);
		double missChance = 1.0 - hitChance;
		double result = 0.0;
		
		while (curChoose < dice) {
			result += nChooseK(dice, curChoose) * 
					Math.pow(hitChance, curChoose) * 
					Math.pow(missChance, dice - curChoose);
			curChoose++;
		}
		result += Math.pow(hitChance, dice);
		
		return result;
	}
	
	/**
	 * This helper method calculates the probability by calculating the chances
	 * of getting too many misses to succeed and then taking 1 - that result.
	 * For (curChoose -> numberOfDice): calculate the chance of getting
	 *    exactly curChoose misses on a roll and add it to a running
	 *    total.
	 * Then return the running total.
	 * Chance of getting exactly curChoose misses:
	 *    (numberOfDice choose curChoose) * 
	 *    (missChance)^(curChoose) * 
	 *    (successChance)^(numberOfDice - curChoose)
	 * @param dice - Number of total dice being rolled
	 * @param successNeeded - Number of successes needed
	 * @param status - Status (normal, blessed, or cursed)
	 * @return - Probability that the roll was successful.
	 */
	private static double calculateByMiss(int dice, int successNeeded, 
			int status) {
		int curChoose = dice - successNeeded + 1;
		double hitChance = 1.0 / ((double)status);
		double missChance = 1.0 - hitChance;
		double result = 0.0;
		
		while (curChoose < dice) {
			result += nChooseK(dice, curChoose) * 
					Math.pow(missChance, curChoose) * 
					Math.pow(hitChance, dice - curChoose);
			curChoose++;
		}
		result += Math.pow(missChance, dice);
		
		return 1 - result;
	}
	
	/**
	 * This helper method reads the radio buttons and returns an int
	 * corresponding to the result.
	 * @return - Denominator of success related to this status.
	 */
	private static int getStatus() {
		if (normalButton.isSelected()) {
			return 3;
		} else if (curseButton.isSelected()) {
			return 6;
		} else {
			return 2;
		}
	}
	
	/**
	 * N choose K - The number of possibilities that one can choose K items out
	 * of N total items. Important for the probability calculation.
	 * N choose K = n! / (k! * (n - k)!)
	 * 
	 * A slight optimization is performed here. If x > y then x! / y! is equal
	 * to the product of all integers going from (y + 1) to x. Thus, we search
	 * first for which is greater, k or (n - k), and take that out of n! in this
	 * manner. Thus we only calculate one full factorial and one partial 
	 * factorial.
	 * @param n - The number of total items
	 * @param k - The number of items to choose
	 * @return - N choose K
	 */
	private static int nChooseK(int n, int k) {
		int numerator = 0;
		int denominator = 1;
		int startUp = 0;
		int denomStop;
		
		if (k == 0 || n == k) {
			return 1;
		}
		
		if (k > (n / 2)) {
			startUp = k + 1;
			denomStop = n - k;
		} else {
			startUp = n - k + 1;
			denomStop = k;
		}
		
		numerator = startUp;
		startUp++;
		while (startUp <= n) {
			numerator = numerator * startUp;
			startUp++;
		}
		
		for (int i = 2; i <= denomStop; i++) {
			denominator = denominator * i;
		}
		
		return (numerator / denominator);
	}
	
	/**
	 * This method initializes the UI by setting up the frame and calling three
	 * helper methods to handle the rest of the UI.
	 */
	private static void initializeUI() {
        frame = new JFrame("ArkhamCalculator");
        frame.setSize(700, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        
        initializeLabels();
        initializeInputs();
        initializeButton();

        frame.setVisible(true);
	}
	
	/**
	 * This method initializes the labels by setting their default values and
	 * placing them on the frame.
	 */
	private static void initializeLabels() {
		numDiceLabel = new JLabel("Number of Dice:");
		numSuccessLabel = new JLabel("Number of Successes Needed:");
		blessCurseLabel = new JLabel("Blessed or Cursed?");
		chanceLabel = new JLabel("Chance of Success:");
		chancePercent = new JLabel("Test %");
		
		numDiceLabel.setBounds(25, 25, 180, 25);
		numSuccessLabel.setBounds(350, 25, 180, 25);
		blessCurseLabel.setBounds(25, 75, 180, 25);
		chanceLabel.setBounds(25, 125, 180, 25);
		chancePercent.setBounds(230, 125, 180, 25);
		
		frame.add(numDiceLabel);
		frame.add(numSuccessLabel);
		frame.add(blessCurseLabel);
		frame.add(chanceLabel);
		frame.add(chancePercent);
	}
	
	/**
	 * This method intializes the inputs by setting their default values and
	 * setting them on the frame.
	 */
	private static void initializeInputs() {
		numDice = new SpinnerNumberModel(1, 1, 20, 1);
		numSuccess = new SpinnerNumberModel(1, 1, 20, 1);
		numDiceSpin = new JSpinner(numDice);
		numSuccessSpin = new JSpinner(numSuccess);
		
		blessButton = new JRadioButton("Blessed");
		normalButton = new JRadioButton("Normal");
		curseButton = new JRadioButton("Cursed");
		statusGroup = new ButtonGroup();
		normalButton.setSelected(true);
		
		statusGroup.add(normalButton);
		statusGroup.add(curseButton);
		statusGroup.add(blessButton);
		
		numDiceSpin.setBounds(230, 25, 45, 25);
		numSuccessSpin.setBounds(555, 25, 45, 25);
		blessButton.setBounds(480, 75, 100, 25);
		normalButton.setBounds(230, 75, 100, 25);
		curseButton.setBounds(355, 75, 100, 25);
		
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				performCalculation();
			} 
		};
		numDiceSpin.addChangeListener(changeListener);
		numSuccessSpin.addChangeListener(changeListener);
		blessButton.addChangeListener(changeListener);
		normalButton.addChangeListener(changeListener);
		curseButton.addChangeListener(changeListener);
		
		frame.add(numDiceSpin);
		frame.add(numSuccessSpin);
		frame.add(blessButton);
		frame.add(normalButton);
		frame.add(curseButton);
	}
	
	/**
	 * This method initializes the calculate button by setting up its action
	 * listener and placing it on the frame.
	 */
	private static void initializeButton() {
		calcButton = new JButton("Calculate");
		calcButton.setBounds(275, 175, 125, 25);
		calcButton.setActionCommand("calculate");
		calcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("calculate".equals(e.getActionCommand())) {
					performCalculation();
				}
			} 
		});
		
		frame.add(calcButton);
	}
	

}
