package QuanNMSP11Sep10;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;
import java.awt.event.WindowEvent;
import javax.swing.event.MouseInputListener;


//JToggleButton but I gave them IDs
class TButton_State extends JToggleButton {
	int ID;
	ActionListener AL;

	public TButton_State(int ID, ActionListener AC) {
		this.ID = ID;
		addActionListener(AC);
	}

	public TButton_State(int ID, ActionListener AC, String Text) {
		this.ID = ID;
		addActionListener(AC);
		setText(Text);
	}
}


//The listener that move the windows when they get dragged
class Mouse_Drag implements MouseInputListener{
	private int dx = 0, dy = 0;
	private DragButton host;

	Mouse_Drag(DragButton host) {
		this.host = host;
	}

	@Override
	public void mousePressed(MouseEvent me){
		dx = me.getX(); 
		dy = me.getY();
		host.getFocus();
	}

	@Override
	public void mouseDragged(MouseEvent me){
		if (me.getX() != dx || me.getY() != dy) host.setPos(host.posX + me.getX() - dx, host.posY + me.getY() - dy);
	}

	@Override
	public void mouseReleased(MouseEvent me){ }

	@Override
	public void mouseClicked(MouseEvent me){ }

	@Override
	public void mouseMoved(MouseEvent me){}

	@Override
	public void mouseEntered(MouseEvent me){}

	@Override
	public void mouseExited(MouseEvent me){}
}


//The windows that do the calculation
class GuI extends JPanel {
	//InitState is kinda redundant here, but if these windows have multiple stages or interfaces it's gonna be very useful
	int ID, posX, posY, InitState = 0, BaseIn = 0, BaseOut = 0;
	JLabel oprName;
	TButton_State b1[], b2[];
	JButton ButtonConvert, ButtonClose, ButtonLogClear, ButtonDrag;
	JTextField In, Out, Log;
	ActionListener listener_State, listener_Convert, listener_LogClear, listener_Close;



	//String numeric addition
	public String customAdd(String s1, String s2) {
		if (!s1.matches("^[-]?[\\d]+$") || !s2.matches("^[-]?[\\d]+$")) return "E";
		else {
			String Out = "";
			int Carry = 0;
			boolean neg1 = false, neg2 = false, neg3 = false;

			if (s1.charAt(0) == '-') {
				neg1 = true;
				s1 = s1.substring(1);
			}

			if (s2.charAt(0) == '-') {
				neg2 = true;
				s2 = s2.substring(1);
			}

			while (s1.length() < s2.length()) s1 = "0" + s1;
			while (s2.length() < s1.length()) s2 = "0" + s2;
			s1 = "0" + s1;
			s2 = "0" + s2;

			if (neg1 && !neg2 && s1.compareTo(s2) > 0) {
				neg1 = false;
				neg2 = true;
				neg3 = true;
			}
			else if (neg2 && !neg1 && s2.compareTo(s1) > 0) {
				neg1 = true;
				neg2 = false;
				neg3 = true;
			}
			else if (neg1 && neg2) {
				neg1 = false;
				neg2 = false;
				neg3 = true;
			}

			int Total = 0;
			for (int i = s1.length() - 1; i > -1; i--) {
				Total = ((neg1)? -((int)s1.charAt(i) - 48): (int)s1.charAt(i) - 48) + ((neg2)? -((int)s2.charAt(i) - 48): (int)s2.charAt(i) - 48) + Carry;
				Carry = 0;

				if (Total > 9) {
					Carry = Total/10;
					Total -= 10;
				}

				if (Total < 0) {
					Carry = -1;
					Total += 10;
				}
				
				Out = Total + Out;
			}
			while (Out.length() > 1 && Out.charAt(0) == '0') Out = Out.substring(1);
			if (Out == "0") return "0";
			else return ((neg3)? "-" : "") + Out;
		}
	}

	public String customDivision(String divident, String divisor, int returnRemainder) {
		if (divident.matches(".*[^\\d].*") || divisor.matches(".*[^\\d].*") || divisor.matches("^0+$")) return "E";
		else if (divident.matches("^0+$")) return "0";
		else {
			String Quotient = "", Remainder = "", addToQuotient = "";
			for (int i = 0; i < divident.length(); i++){
				Remainder += divident.charAt(i);
				while (Remainder.length() > 1 && Remainder.charAt(0) == '0') Remainder = Remainder.substring(1);
				if ((Remainder.length() > divisor.length()) ||(Remainder.length() == divisor.length() && Remainder.compareTo(divisor) > -1)) {
					int aTQ = 0;
					String temp = divisor;
					while ((temp.length() < Remainder.length()) || (temp.length() == Remainder.length() && temp.compareTo(Remainder) < 1)) {
						addToQuotient = temp;
						aTQ++;
						temp = customAdd(addToQuotient, divisor);
					}
					Quotient += aTQ;
					Remainder = customAdd(Remainder, "-" + addToQuotient);
				}
				else Quotient += "0";
			}

			while (Remainder.length() > 1 && Remainder.charAt(0) == '0') Remainder = Remainder.substring(1);
			while (Quotient.length() > 1 && Quotient.charAt(0) == '0') Quotient = Quotient.substring(1);
			return (returnRemainder == 1)? Remainder: Quotient;
		}
	}

	//Logics stole from main, adapted and upgraded to fit into windows. The original logic of main that works on the console is kept just in case someone prefers TUI over GUI =))
	public boolean checkFormat(String toCheck, int choice) {
		String num = toCheck.toUpperCase();
		if (InitState > 0) Log.setForeground(Color.red);
		if (choice == 1) {
			for (int i = 0; i < num.length(); i++) if (!(num.charAt(i) == '1' || num.charAt(i) == '0')) 
			{
				if (InitState > 0) Log.setText("Illegal character detected at index " + i + ": " + toCheck.charAt(i));
				return false;
			}
			return true;

		} else if (choice == 2) {
			if (num.matches("[\\d]+")) return true;
			else for (int i = 0; i < num.length(); i++) if (!String.valueOf(num.charAt(i)).matches("\\d")) 
			{
				if (InitState > 0) Log.setText("Illegal character detected at " + i + ": " + toCheck.charAt(i));
				return false;
			}

		} else if (choice == 3) {
			for (int i = 0; i < num.length(); i++) 
				if (! ( ((int)num.charAt(i) < 71 && (int)num.charAt(i) > 47) 
							&& ((int)num.charAt(i) < 58 || (int)num.charAt(i) > 64) ) ) {
				if (InitState > 0) Log.setText("Illegal character detected at " + i + ": " + toCheck.charAt(i));
				return false;
			}
			return true;
		}
		return false;
	}

	public String Convert(int base, int base2, String num) {
		short uhhhh[] = {0, 2, 10, 16};
		System.out.println("\nWindow ID " + ID + ": Initiated converion of \"" + num + "\" from base " + uhhhh[base] + " to base " + uhhhh[base2] + ", checkFormat returns " + checkFormat(num, base));
		if (base == 1 && base2 > 0 && checkFormat(num, base)) {
			if (base2 == 2) {
				String Out = "0";
				for (int i = 0; i < num.length(); i++)
					Out = customAdd(num.charAt(i) + "", customAdd(Out, Out));

				System.out.println("Converion successful, returned: " + Out);
				return Out;
			}
			else if (base2 == 3) {
				String Out = "", frag = "";
				int temp = 0;
				while (num.length() % 4 != 0)
					num = "0" + num;

				for (int i = 0; i * 4 < num.length(); i++) {
					frag = num.substring(i * 4, i * 4 + 4);
					temp = 0;
					for (int ii = 0; ii < 4; ii++) 
						if (frag.charAt(4 - ii - 1) == '1') temp += (int)Math.pow(2, ii);

					if (temp < 10) Out += temp + "";
					else Out += (char)(temp + 55);
				}

				while (Out.length() > 1 && Out.charAt(0) == '0') Out = Out.substring(1);
				System.out.println("Converion successful, returned: " + Out);
				return Out;
			}

			else {
				System.out.println("Converion ignored");
				return num;
			}
		}

		if (base == 2 && base2 > 0 && checkFormat(num, base)) { 
			String Out = "", remainder = "", baseS = "";

			if (!(base2 == 1 || base2 == 3)) {
				System.out.println("Converion ignored");
				return num;
			}
			else {
				if (base2 == 1) baseS = "2";
				if (base2 == 3) baseS = "16";
				Out = "";
				while (!num.equals("0")) {
					remainder = customDivision(num, baseS, 1);
					if (remainder.length() > 1) Out = (char)(remainder.charAt(1) + 17) + Out;
					else Out = (remainder) + Out;
					num = customDivision(num, baseS, 0);
				}

				System.out.println("Converion successful, returned: " + Out);
				return Out;
			}
		}

		if (base == 3 && base2 > 0 && checkFormat(num, base)) {
			if (base2 == 1) {
				num = (new StringBuffer(num)).reverse().toString().toUpperCase();
				String Out = "";
				int temp = 0;
				for (int i = 0; i < num.length(); i++) {
					if ((int)num.charAt(i) < 58) temp = (int)num.charAt(i) - 48;
					else temp = (int)num.charAt(i) - 55;
					if (temp == 0) Out = "0000" + Out;

					while (temp > 0) {
						if (temp % 2 > 9) Out = (char)(temp % 2 + 55) + Out;
						else Out = (temp % 2) + Out;
						temp = temp / 2;
					}
					while (Out.length() % 4 > 0) Out = "0" + Out;
				}
				while (Out.length() > 1 && Out.charAt(0) == '0') Out = Out.substring(1);

				System.out.println("Converion successful, returned: " + Out);
				return Out;
			}
			else if (base2 == 2) {
				num = num.toUpperCase();
				String Out = "0";
				for (int i = 0; i < num.length(); i++) {
					for (int ii = 0; ii < 4; ii++) Out = customAdd(Out, Out);
					if (num.charAt(i) < 58) Out = customAdd(num.charAt(i) + "", Out);
					else Out = customAdd("1" + (char)(num.charAt(i) - 17), Out); 
				}

				System.out.println("Converion successful, returned: " + Out);
				return Out;
			}
			else {
				System.out.println("Converion ignored");
				return num;
			}
		}

		if (InitState > 0 && base2 * base == 0) {
			Log.setForeground(Color.orange);
			if (base2 == 0) Log.setText("You haven't specified the base to convert the number into.");
			if (base == 0) Log.setText("You haven't specified the base of the number.");
		}
		return "";
	}
	////



	GuI(int ID, int posX, int posY) {
		this.ID = ID;
		setBounds(posX, posY, 500, 500); //Change height and width of the windows here
		setBackground(Color.white);
		setLayout(null);
		setBorder(new LineBorder(Color.black, 3));

		b1 = new TButton_State[3];
		b2 = new TButton_State[3];

		//Listen to the base change buttons and change the bases
		listener_State = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					TButton_State btn = (TButton_State)e.getSource();
					if (btn.ID < 20) {
						if (BaseIn > 0) if (btn.ID - 10 == BaseIn) BaseIn = 0;
							else {
								b1[BaseIn - 1].setSelected(false);
								BaseIn = btn.ID - 10;
							}
						else BaseIn = btn.ID - 10;
					}
					else {
						if (BaseOut > 0) if (btn.ID - 20 == BaseOut) BaseOut = 0;
							else {
								b2[BaseOut - 1].setSelected(false);
								BaseOut = btn.ID - 20;
							}
						else BaseOut = btn.ID - 20;
					}
				} 
				catch (Exception ex) {
					Log.setForeground(Color.red);
					Log.setText(ex.toString());
				}
			}
		};

		//The "convert" button
		listener_Convert = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Log.setText("");
					Out.setText(Convert(BaseIn, BaseOut, In.getText()));
				} 
				catch (Exception ex) {
					Log.setForeground(Color.red);
					Log.setText(ex.toString());
				}
			}
		};

		//The Log clear button
		listener_LogClear = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (InitState > 0) Log.setText("");
			}
		};

		//The "Close" button
		listener_Close = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (QuanNMSP11Sep10.InitState > 0) QuanNMSP11Sep10.gRemove(ID);
			}
		};
		InitState++;

		//Switch this window to the mainMenu State, which is also it's only state =))
		mainMenu();
	}



	public void mainMenu() {
		Log = new JTextField();
		Log.setBounds(3, 477, 440, 20);
		Log.setEditable(false);
		add(Log);

		ButtonLogClear = new JButton("C");
		ButtonLogClear.setBounds(447, 477, 50, 20);
		ButtonLogClear.setFont(new Font("Applet", Font.PLAIN, 15));
		ButtonLogClear.addActionListener(listener_LogClear);
		add(ButtonLogClear);

		oprName = new JLabel("======= Number base conversion ========");
		oprName.setBounds(-5, 25, 550, 20);
		oprName.setFont(new Font("Arial", Font.PLAIN, 25));
		add(oprName);

		b1[0] = new TButton_State(11, listener_State);
		b1[0].setText("1. Binary (base 2)");
		b1[0].setBounds(5, 100, 195, 50);
		b1[0].setFont(new Font("Arial", Font.PLAIN, 15));
		b1[0].setBackground(new Color(14474460));
		add(b1[0]);

		b1[1] = new TButton_State(12, listener_State);
		b1[1].setText("2. Decimal (base 10)");
		b1[1].setBounds(5, 175, 195, 50);
		b1[1].setFont(new Font("Arial", Font.PLAIN, 15));
		b1[1].setBackground(new Color(14474460));
		add(b1[1]);

		b1[2] = new TButton_State(13, listener_State);
		b1[2].setText("3. Hexadecimal (base 16)");
		b1[2].setBounds(5, 250, 195, 50);
		b1[2].setFont(new Font("Arial", Font.PLAIN, 12));
		b1[2].setBackground(new Color(14474460));
		add(b1[2]);

		b2[0] = new TButton_State(21, listener_State);
		b2[0].setText("1. Binary (base 2)");
		b2[0].setBounds(300, 100, 195, 50);
		b2[0].setFont(new Font("Arial", Font.PLAIN, 15));
		b2[0].setBackground(new Color(14474460));
		add(b2[0]);

		b2[1] = new TButton_State(22, listener_State);
		b2[1].setText("2. Decimal (base 10)");
		b2[1].setBounds(300, 175, 195, 50);
		b2[1].setFont(new Font("Arial", Font.PLAIN, 15));
		b2[1].setBackground(new Color(14474460));
		add(b2[1]);

		b2[2] = new TButton_State(23, listener_State);
		b2[2].setText("3. Hexadecimal (base 16)");
		b2[2].setBounds(300, 250, 195, 50);
		b2[2].setFont(new Font("Arial", Font.PLAIN, 12));
		b2[2].setBackground(new Color(14474460));
		add(b2[2]);

		In = new JTextField(1);
		In.setBounds(10, 325, 175, 50);
		In.setFont(new Font("Arial", Font.PLAIN, 25));
		In.setBorder(new LineBorder(Color.blue));
		add(In);

		Out = new JTextField(1);
		Out.setBounds(315, 325, 175, 50);
		Out.setEditable(false);
		Out.setFont(new Font("Arial", Font.PLAIN, 25));
		Out.setBorder(new LineBorder(Color.blue));
		add(Out);

		ButtonConvert = new JButton("Convert");
		ButtonConvert.setBounds(40, 385, 110, 30);
		ButtonConvert.setFont(new Font("Arial", Font.PLAIN, 12));
		ButtonConvert.setBackground(new Color(23948));
		ButtonConvert.setForeground(Color.white);
		ButtonConvert.addActionListener(listener_Convert);
		add(ButtonConvert);

		ButtonClose = new JButton("Close");
		ButtonClose.setBounds(350, 385, 110, 30);
		ButtonClose.setFont(new Font("Arial", Font.PLAIN, 12));
		ButtonClose.setBackground(Color.red);
		ButtonClose.setForeground(Color.white);
		ButtonClose.addActionListener(listener_Close);
		add(ButtonClose);

		JLabel TextTo = new JLabel("To");
		TextTo.setBounds(235, 185, 195, 30);
		TextTo.setFont(new Font("Arial", Font.PLAIN, 30));
		TextTo.setBackground(new Color(14474460));
		add(TextTo);
		JLabel TextEql = new JLabel("=");
		TextEql.setBounds(235, 330, 195, 30);
		TextEql.setFont(new Font("Arial", Font.PLAIN, 30));
		TextEql.setBackground(new Color(14474460));
		add(TextEql);

		if (QuanNMSP11Sep10.InitState > 0) {
			QuanNMSP11Sep10.frame.validate();
			QuanNMSP11Sep10.frame.repaint();
		}
		InitState++;
	}
}


//An invisible button that hides just below a window (GuI). Will catch events when you click on an area that's not a button on the windows.
class DragButton extends JButton{
	GuI host;
	int posX, posY;
	Mouse_Drag listener_OnDrag;

	public DragButton(GuI host){
		this.host = host;
		host.ButtonDrag = this;
		setBounds(host.getBounds());
		posX = host.getBounds().x;
		posY = host.getBounds().y;
		setContentAreaFilled(false);	

		listener_OnDrag = new Mouse_Drag(this);
		addMouseMotionListener(listener_OnDrag);
		addMouseListener(listener_OnDrag);
	}

	public void setPos(int x, int y) {
		posX = x;
		posY = y;

		host.setLocation(posX, posY);
		setBounds(host.getBounds());
	}

	public void getFocus() {
		if (QuanNMSP11Sep10.InitState > 0) QuanNMSP11Sep10.focusOn(this.host);
	}
}


//Main class
public class QuanNMSP11Sep10 {
	public static JLayeredPane plane;
	public static JFrame frame;
	public static JButton Button_CreateGuI, Button_Quit;
	public static int InitState = 0, highID = 0, highLayer = 2;
	public static java.util.List<GuI> OPanelList = new ArrayList<>();

	//Creates the frame, which is the base of the whole GUI thing. It's in funtion form so that the code is cleaner and more customizable.
	public static void signalStart() {
		plane = new JLayeredPane();
		frame = new JFrame();

		Button_CreateGuI = new JButton("Create an Instance");
		Button_CreateGuI.setFont(new Font("Arial", Font.PLAIN, 18));
		Button_CreateGuI.setBackground(new Color(18879));
		Button_CreateGuI.setForeground(Color.white);

		Button_Quit = new JButton("Quit");
		Button_Quit.setFont(new Font("Arial", Font.PLAIN, 18));
		Button_Quit.setBackground(Color.red);
		Button_Quit.setForeground(Color.white);

		frame.setTitle("J1.S.0011 by Quan NM HE182223");
		frame.setSize(1000, 1000);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMaximumSize(new Dimension(1000, 1000));
		frame.setVisible(true);
		frame.setContentPane(plane);

		Button_CreateGuI.setBounds(10, frame.getHeight() - 80, 200, 50);
		plane.add(Button_CreateGuI);
		plane.setLayer(Button_CreateGuI, 0);

		Button_CreateGuI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createGuI(0, 0);
			}
		});

		Button_Quit.setBounds(230, frame.getHeight() - 80, 200, 50);
		plane.add(Button_Quit);
		plane.setLayer(Button_Quit, 0);

		Button_Quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				signalClose();
			}
		});

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Button_CreateGuI.setBounds(10, frame.getHeight() - 80, 200, 50);
				Button_Quit.setBounds(230, frame.getHeight() - 80, 100, 50);
			}
		});

		InitState = 1;
	}


	//Creates a new instance of the windows.
	public static void createGuI(int x, int y) {
		if (InitState > 0) {
			GuI newGuI = new GuI(highID, x, y);
			OPanelList.add(newGuI);
			DragButton newDrag = new DragButton(newGuI);
			newGuI.ButtonDrag = newDrag;

			plane.add(newGuI);
			plane.setLayer(newGuI, highLayer);
			plane.add(newDrag);
			plane.setLayer(newDrag, highLayer - 1);

			System.out.println("Created new conversion window with ID: " + highID);
			highID++;
			highLayer = highLayer + 2;
			plane.setLayer(Button_CreateGuI, highLayer + 2);
			plane.setLayer(Button_Quit, highLayer + 2);
		}
	}


	//Focus on a window (render it as the highest).
	public static void focusOn(GuI gui) {
		plane.setLayer(gui, highLayer);
		plane.setLayer(gui.ButtonDrag, highLayer - 1);

		highLayer = highLayer + 2;
		plane.setLayer(Button_CreateGuI, highLayer + 2);
		plane.setLayer(Button_Quit, highLayer + 2);
	}


	//Close the whole program.
	public static void signalClose() {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}


	//Remove a window with a specified ID.
	public static void gRemove(int ID) {
		OPanelList.forEach(g -> { if (g.ID == ID) {frame.remove(g.ButtonDrag); frame.remove(g);} });
		System.out.println("Window ID " + ID + " closed successfully");
		frame.repaint();
		frame.revalidate();
	}


	public static void prt(String toPrt, int l1, int l2) {
		System.out.print( ((l1 == 1)? "\n" : "") + toPrt + ((l2 == 1)? "\n" : "") );
	}


	//Old conversion code.
	public static boolean checkFormat(String toCheck, int choice) {
		String num = toCheck.toUpperCase();
		if (choice == 1) {
			for (int i = 0; i < num.length(); i++) if (!(num.charAt(i) == '1' || num.charAt(i) == '0')) 
			{
				prt("Illegal character detected at " + i + ": " + toCheck.charAt(i), 1, 0); 
				return false;
			}
			return true;

		} else if (choice == 2) {
			return !num.matches("[^0-9]");

		} else if (choice == 3) {
			for (int i = 0; i < num.length(); i++) 
				if (! ( ((int)num.charAt(i) < 71 && (int)num.charAt(i) > 47) 
							&& ((int)num.charAt(i) < 58 || (int)num.charAt(i) > 64) ) ) {
				prt("Illegal character detected at " + i + ": " + toCheck.charAt(i), 1, 0); 
				return false;
			}
			return true;
		}
		return false;
	}

	public static void main(String args[]) {
		signalStart();
		Scanner sc = new Scanner(System.in);

		prt("GUI logs (Enter any character to start TUI session):", 1, 1);
		sc.nextLine();
		prt("======= Number base conversion ========", 1, 0);
		prt("Please note that the operation of converting decimal to other bases in the terminal interface still adhere to the integer size limit. For large number conversion (>2147483647), please use the graphical interface (too lazy to tweak the old TUI lol).", 1, 0);
		prt("Choose the base of the original number:", 1, 0) ;
		prt("1. Binary (base 2)", 1, 0);
		prt("2. Decimal (base 10)", 1, 0);
		prt("3. Hexadecimal (base 16)", 1, 0);
		prt("4. Quit", 1,  0);
		prt("5. CLear the console\n> ", 1,  0);

		int choice = 6;
		try {
			choice = Integer.parseInt(sc.nextLine());
		}
		catch (Exception e) {
			prt("Invalid option.", 1, 0);
			choice = 6;
		}


		while (choice != 4) {
			int choice2 = 6;
			if (choice != 5) {
				while (choice != 6 && choice2 == 6) {
					prt("Choose the base to convert the number into:", 1, 0);
					prt("1. Binary (base 2)", 1, 0);
					prt("2. Decimal (base 10)", 1, 0);
					prt("3. Hexadecimal (base 16)\n> ", 1, 0);
					try {
						choice2 = Integer.parseInt(sc.nextLine());
					}
					catch (Exception e) {
						choice2 = 6;
					}
					prt("Enter the number to convert: ", 1, 0);
				}
			}

			
			if (choice == 1) {
				String num = sc.nextLine();
				while (!checkFormat(num, choice)) {
					prt("please re-enter the number: ", 1, 0);
					num = sc.nextLine();
				}
				if (choice2 == 2) {
					int Out = 0;
					for (int i = 0; i < num.length(); i++) 
						if (num.charAt(num.length() - i - 1) == '1') Out += (int)Math.pow(2, i);

					prt("Result: " + Out, 1, 1);
				}
				else if (choice2 == 3) {
					String Out = "", frag = "";
					int temp = 0;
					while (num.length() % 4 != 0)
						num = "0" + num;

					for (int i = 0; i * 4 < num.length(); i++) {
						frag = num.substring(i * 4, i * 4 + 4);
						temp = 0;
						for (int ii = 0; ii < 4; ii++) 
							if (frag.charAt(4 - ii - 1) == '1') temp += (int)Math.pow(2, ii);

						if (temp < 10) Out += temp + "";
						else Out += (char)(temp + 55);
					}

					prt("Result: " + Out, 1, 1);
				}
				else prt("Number base chosen was not a listed option or is the same as the original number's base.", 1, 0);
			}

			if (choice == 2) {
				String Out = sc.nextLine();
				while (!checkFormat(Out, choice)) {
					prt("please re-enter the number: ", 1, 0);
					Out = sc.nextLine();
				}
				int num = Integer.parseInt(Out);

				if (!(choice2 == 1 || choice2 == 3)) prt("Number base chosen was not a listed option or is the same as the original number's base.", 1, 0);
				else {
					if (choice2 == 1) choice2 = 2;
					if (choice2 == 3) choice2 = 16;
					Out = "";
					while (num > 0) {
						if (num % choice2 > 9) Out = (char)(num % choice2 + 55) + Out;
						else Out = (num % choice2) + Out;
						num = num / choice2;
					}

					prt("Result: " + Out, 1, 1);
				}
			}

			if (choice == 3) {
				String num = sc.nextLine();
				num = num.toUpperCase();
				while (!checkFormat(num, choice)) {
					prt("please re-enter the number: ", 1, 0);
					num = sc.nextLine();
					num = num.toUpperCase();
				}

				if (choice2 == 1) {
					num = (new StringBuffer(num)).reverse().toString();
					String Out = "";
					int temp = 0;
					for (int i = 0; i < num.length(); i++) {
						if ((int)num.charAt(i) < 58) temp = (int)num.charAt(i) - 48;
						else temp = (int)num.charAt(i) - 55;

						while (temp > 0) {
							if (temp % 2 > 9) Out = (char)(temp % 2 + 55) + Out;
							else Out = (temp % 2) + Out;
							temp = temp / 2;
						}

						while (Out.length() % 4 > 0) Out = "0" + Out;
					}
					prt("Result: " + Out, 1, 1);
				}

				if (choice2 == 2) {
					int Out = 0, temp = 0;
					for (int i = 0; i < num.length(); i++) {
						if ((int)num.charAt(i) < 58) temp = (int)num.charAt(i) - 48;
						else temp = (int)num.charAt(i) - 55;

						Out += temp * (int)Math.pow(16, num.length() - i - 1);
					}

					prt("Result: " + Out, 1, 1);
				}
			}

			if (choice == 5) prt("\033[H\033[2J", 0, 0);

			prt("Choose the base of the original number:", 1, 0);
			prt("1. Binary (base 2)", 1, 0);
			prt("2. Decimal (base 10)", 1, 0);
			prt("3. Hexadecimal (base 16)", 1, 0);
			prt("4. Quit", 1,  0);
			prt("5. CLear the console\n> ", 1,  0);
			try {
				choice = Integer.parseInt(sc.nextLine());
			}
			catch (Exception e) {
				prt("Invalid option.", 1, 0);
				choice = 6;
			}
		}
		signalClose();
	}
}
