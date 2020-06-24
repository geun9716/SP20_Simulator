package SP20_simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator extends JFrame{
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);
	
	NorthPane northpane;
	SouthPane southpane;
	WestPane westpane;
	EastPane eastpane;
	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program){
		//...
		sicLoader.load(program);
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep(){
		sicSimulator.oneStep();
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep(){
		sicSimulator.allStep();
		
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update(){
		//프로그램 이름, 프로그램 시작주소, 프로그램 길이
		westpane.PRO_NAME.setText(resourceManager.ProName.get(0));
		westpane.PRO_ADDR.setText(String.format("%06X", resourceManager.getStartAddr(0)));
		westpane.PRO_LENGTH.setText(Integer.toHexString(resourceManager.ProLength));
		//레지스터 A
		westpane.A_dec.setText(Integer.toString(resourceManager.register[0]));
		westpane.A_hex.setText(String.format("%06X", resourceManager.register[0]));
		//레지스터 X
		westpane.X_dec.setText(Integer.toString(resourceManager.register[1]));
		westpane.X_hex.setText(String.format("%06X", resourceManager.register[1]));
		//레지스터 L
		westpane.L_dec.setText(Integer.toString(resourceManager.register[2]));
		westpane.L_hex.setText(String.format("%06X", resourceManager.register[2]));
		//레지스터 B
		westpane.B_dec.setText(Integer.toString(resourceManager.register[3]));
		westpane.B_hex.setText(String.format("%06X", resourceManager.register[3]));
		//레지스터 S
		westpane.S_dec.setText(Integer.toString(resourceManager.register[4]));
		westpane.S_hex.setText(String.format("%06X", resourceManager.register[4]));
		//레지스터 T
		westpane.T_dec.setText(Integer.toString(resourceManager.register[5]));
		westpane.T_hex.setText(String.format("%06X", resourceManager.register[5]));
		//레지스터 F
		westpane.F_result.setText(Double.toString(resourceManager.register_F));
		//레지스터 PC
		westpane.PC_dec.setText(Integer.toString(resourceManager.register[8]));
		westpane.PC_hex.setText(String.format("%06X", resourceManager.register[8]));
		//레지스터 SW
		westpane.SW_result.setText(Integer.toString(resourceManager.register[9]));
		
		eastpane.END_ADDR.setText(String.format("%06X", resourceManager.EndRecord.get(0)));
		eastpane.StartAddrMem.setText(Integer.toString(resourceManager.getStartAddr(0)));
		
		//Instructions
		eastpane.model.clear();
		for(int i = 0 ; i < resourceManager.Instruction.size(); i++)
		{
			eastpane.model.addElement(resourceManager.Instruction.get(i));
		}
		eastpane.Insts.setSelectedIndex(resourceManager.Instruction.size()-1);
		eastpane.Insts.ensureIndexIsVisible(eastpane.Insts.getSelectedIndex());
		
		//Target Address
		eastpane.TA.setText(String.format("%-4X", resourceManager.TA));
		
		//Using Device
		eastpane.device.setText(resourceManager.device);
		
		if(resourceManager.isFinish)
		{
			eastpane.ex_step.setEnabled(false);
			eastpane.ex_all.setEnabled(false);
		}
		//Log
		southpane.textarea_s.setText("");
		for(int i = 0 ; i < resourceManager.Log.size(); i++)
		{
			southpane.textarea_s.append(resourceManager.Log.get(i) + "\n");
		}
		southpane.textarea_s.setCaretPosition(southpane.textarea_s.getDocument().getLength());

	};

	public static void main(String[] args) {
		VisualSimulator vs = new VisualSimulator();
		
	}
	
	public VisualSimulator(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 770);
		
		Container contentPane = this.getContentPane();
		setLayout(new BorderLayout());
		
		
		//North Pane
		northpane = new NorthPane();
		//South Pane
		southpane = new SouthPane();
		//Center Pane
		JPanel centerpane = new JPanel();
		westpane = new WestPane();
		eastpane = new EastPane();
		
		centerpane.add(westpane);
		centerpane.add(eastpane);
		contentPane.add(northpane, BorderLayout.NORTH);
		contentPane.add(southpane, BorderLayout.SOUTH);
		contentPane.add(centerpane, BorderLayout.CENTER);
		
		//Open에 대한 클릭 리스너 : 파일을 선택하고 load한다.
		northpane.button_n.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				FileDialog fileOpen = new FileDialog(VisualSimulator.this, "파일열기", FileDialog.LOAD);
	            fileOpen.setDirectory("./");
	            fileOpen.setVisible(true);
	            northpane.textfield_n.setText(fileOpen.getFile());
	            try {
	            	File input = new File(fileOpen.getDirectory()+fileOpen.getFile());
		            load(input);
		            update();
		            northpane.button_n.setEnabled(false);
	            } catch (NullPointerException ne) {
	            	ne.printStackTrace();
	            }
			}
		});
		
		//1 Step 실행에 대한 클릭 리스너 
		eastpane.ex_step.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				oneStep();
				update();
			}
		});
		//All Step 실행에 대한 클릭 리스너
		eastpane.ex_all.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				allStep();
				update();
			}
		});
		//Exit에 대한 클릭 리스너
		eastpane.exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				resourceManager.closeDevice();
				System.exit(0);
			}
		});
		
		setVisible(true);
	}
	
	public class NorthPane extends JPanel{
		/**
		 * 파일 선택 컴포넌트를 가진 패널
		 */
		private static final long serialVersionUID = 1L;
		JTextField textfield_n;
		JButton button_n;
		NorthPane(){
			JLabel label_n = new JLabel("FileName: ");
			textfield_n = new JTextField(10);
			button_n = new JButton("open");
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
			this.add(label_n);
			this.add(textfield_n);
			this.add(button_n);
		}
	}
	public class SouthPane extends JPanel{
		/**
		 * Log에 대한 TextArea
		 */
		
		private static final long serialVersionUID = 1L;
		JTextArea textarea_s;
		SouthPane(){
			this.setLayout(new GridBagLayout());
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			GridBagConstraints gbc1 = new GridBagConstraints();
			
			JLabel label_s = new JLabel("Log(명령어 수행 관련):");
			textarea_s = new JTextArea(10, 30);
			JScrollPane scrollPane = new JScrollPane(textarea_s);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
			gbc1.anchor = GridBagConstraints.WEST;
			this.add(label_s, gbc1);
			
			gbc1.fill = GridBagConstraints.BOTH;
			gbc1.gridy = 1;
			gbc1.weightx = 1;
			gbc1.weighty = 1;
			this.add(scrollPane, gbc1);
		}
	}
	public class WestPane extends JPanel{
		/**
		 * 프로그램 이름, 프로그램 시작주소, 프로그램 길이를 
		 */
		private static final long serialVersionUID = 1L;
		JLabel PRO_NAME;
		JLabel PRO_ADDR;
		JLabel PRO_LENGTH;
		JLabel A_dec, A_hex, X_dec, X_hex, L_dec, L_hex, B_dec, B_hex;
		JLabel S_dec, S_hex, T_dec, T_hex, F_result, PC_dec, PC_hex, SW_result;
		WestPane(){
			this.setLayout(new GridBagLayout());
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			JPanel p1 = new JPanel();
			JPanel p2 = new JPanel();
			TitledBorder b1 = new TitledBorder("H (Header Record)");
			TitledBorder b2 = new TitledBorder("Register");
			
			
			p1.setBorder(b1);
			p2.setBorder(b2);
			
			
			//Panel : Header Record
			p1.setLayout(new GridBagLayout());
			GridBagConstraints gbc1 = new GridBagConstraints();
			gbc1.fill = GridBagConstraints.HORIZONTAL;
			gbc1.insets = new Insets(0, 0, 5, 0);
			
			JLabel l1 = new JLabel("Program name: ");
			PRO_NAME = new JLabel("");
			PRO_NAME.setPreferredSize(new Dimension(100, 20));
			PRO_NAME.setSize(WIDTH, 30);
			PRO_NAME.setBorder(BorderFactory.createLineBorder(Color.lightGray));

			gbc1.gridx = 0;
			gbc1.gridy = 0;
			gbc1.gridwidth = 1;
			p1.add(l1,gbc1);
			gbc1.gridx = 1;
			gbc1.gridy = 0;
			gbc1.gridwidth = 1;
			p1.add(PRO_NAME,gbc1);
			
			JLabel l2 = new JLabel("<html>Start Address of<br>Object Program: </html>");
			PRO_ADDR = new JLabel("");
			PRO_ADDR.setPreferredSize(new Dimension(100, 20));
			PRO_ADDR.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			gbc1.gridx = 0;
			gbc1.gridy = 1;
			gbc1.gridwidth = 1;
			p1.add(l2, gbc1);
			gbc1.gridx = 1;
			gbc1.gridy = 1;
			gbc1.gridwidth = GridBagConstraints.REMAINDER;
			p1.add(PRO_ADDR, gbc1);
			
			JLabel l3 = new JLabel("Length of Program: ");
			PRO_LENGTH = new JLabel("");
			PRO_LENGTH.setPreferredSize(new Dimension(100, 20));
			PRO_LENGTH.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			gbc1.gridx = 0;
			gbc1.gridy = 2;
			gbc1.gridwidth = 1;
			p1.add(l3, gbc1);
			gbc1.gridx = 1;
			gbc1.gridy = 2;
			gbc1.gridwidth = GridBagConstraints.REMAINDER;
			p1.add(PRO_LENGTH);
			
			
			
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.insets = new Insets(0, 0, 10 ,0);
			this.add(p1, gbc);
			
			//Panel : register
			
			p2.setLayout(new GridBagLayout());
			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.fill = GridBagConstraints.BOTH;
			
			gbc2.gridx = 1;
			gbc2.gridy = 0;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel l4 = new JLabel("Dec");
			p2.add(l4, gbc2);
			
			gbc2.gridx = 2;
			gbc2.gridy = 0;
			gbc2.insets = new Insets(0, 0, 5, 0);
			JLabel l5 = new JLabel("Hex");
			p2.add(l5, gbc2);
			
			// A register
			gbc2.gridx = 0;
			gbc2.gridy = 1;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel A = new JLabel("A (#0)", JLabel.RIGHT);
			p2.add(A, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			A_dec = new JLabel("");
			A_dec.setPreferredSize(new Dimension(70, 10));
			A_dec.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(A_dec, gbc2);
			
			gbc2.gridx = 2;
			gbc2.gridy = 1;
			gbc2.insets = new Insets(0, 0, 5, 0);
			A_hex = new JLabel("");
			A_hex.setPreferredSize(new Dimension(70, 10));
			A_hex.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(A_hex, gbc2);
			
			// X register
			gbc2.gridx = 0;
			gbc2.gridy = 2;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel X = new JLabel("X (#1)", JLabel.RIGHT);
			p2.add(X, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 2;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			X_dec = new JLabel("");
			X_dec.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(X_dec, gbc2);
			
			gbc2.gridx = 2;
			gbc2.gridy = 2;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 0);
			X_hex = new JLabel("");
			X_hex.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(X_hex, gbc2);
			
			// L register
			gbc2.gridx = 0;
			gbc2.gridy = 3;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel L = new JLabel("L (#2)", JLabel.RIGHT);
			p2.add(L, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 3;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			L_dec = new JLabel("");
			L_dec.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(L_dec, gbc2);
			
			gbc2.gridx = 2;
			gbc2.gridy = 3;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 0);
			L_hex = new JLabel("");
			L_hex.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(L_hex, gbc2);
			
			//B register
			gbc2.gridx = 0;
			gbc2.gridy = 4;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel B = new JLabel("B (#3)", JLabel.RIGHT);
			p2.add(B, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 4;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			B_dec = new JLabel("");
			B_dec.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(B_dec, gbc2);
			
			gbc2.gridx = 2;
			gbc2.gridy = 4;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 0);
			B_hex = new JLabel("");
			B_hex.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(B_hex, gbc2);
			
			//S register
			gbc2.gridx = 0;
			gbc2.gridy = 5;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel S = new JLabel("S (#4)", JLabel.RIGHT);
			p2.add(S, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 5;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			S_dec = new JLabel("");
			S_dec.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(S_dec, gbc2);
			
			gbc2.gridx = 2;
			gbc2.gridy = 5;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 0);
			S_hex = new JLabel("");
			S_hex.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(S_hex, gbc2);
			
			//T register
			gbc2.gridx = 0;
			gbc2.gridy = 6;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel T = new JLabel("T (#5)", JLabel.RIGHT);
			p2.add(T, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 6;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			T_dec = new JLabel("");
			T_dec.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(T_dec, gbc2);
			
			gbc2.gridx = 2;
			gbc2.gridy = 6;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 0);
			T_hex = new JLabel("");
			T_hex.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(T_hex, gbc2);
			
			//F register
			gbc2.gridx = 0;
			gbc2.gridy = 7;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel F = new JLabel("F (#6)", JLabel.RIGHT);
			p2.add(F, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 7;
			gbc2.gridwidth = 2;
			gbc2.insets = new Insets(0, 0, 5, 0);
			F_result = new JLabel("");
			F_result.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(F_result, gbc2);
			
			//PC register
			gbc2.gridx = 0;
			gbc2.gridy = 8;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel PC = new JLabel("PC (#8)", JLabel.RIGHT);
			p2.add(PC, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 8;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			PC_dec = new JLabel("");
			PC_dec.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(PC_dec, gbc2);
			
			gbc2.gridx = 2;
			gbc2.gridy = 8;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 0);
			PC_hex = new JLabel("");
			PC_hex.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(PC_hex, gbc2);
			
			//SW register
			gbc2.gridx = 0;
			gbc2.gridy = 9;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			JLabel SW = new JLabel("SW (#9)", JLabel.RIGHT);
			p2.add(SW, gbc2);
			
			gbc2.gridx = 1;
			gbc2.gridy = 9;
			gbc2.gridwidth = 2;
			gbc2.insets = new Insets(0, 0, 5, 0);
			SW_result = new JLabel("");
			SW_result.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			p2.add(SW_result, gbc2);
			
			
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 1;
			gbc.weighty = 1;
			this.add(p2, gbc);
			
		}
		
	}
	public class EastPane extends JPanel{

		/**
		 * 프로그램의 End 레코드 , 프로그램 메모리 시작주소, TargetAddress, Instructions, 사용중인 장치, 실행 버튼들의 대한 컴포넌트를 가진다.
		 */
		private static final long serialVersionUID = 1L;
		
		JLabel END_ADDR;
		JLabel StartAddrMem;
		JLabel TA;
		JList Insts;
		DefaultListModel model;
		JScrollPane scrollPane;
		JLabel device;
		JButton ex_step;
		JButton ex_all;
		JButton exit;
		EastPane(){
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			this.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			
			
			//End Record panel
			JPanel p1 = new JPanel();
			TitledBorder b1 = new TitledBorder("E (End Record)");
			p1.setBorder(b1);
			p1.setLayout(new GridBagLayout());
			GridBagConstraints gbc1 = new GridBagConstraints();
			gbc1.fill = GridBagConstraints.BOTH;
			
			JLabel l1 = new JLabel("Address of First instruction             ");
			gbc1.gridx = 0;
			gbc1.gridy = 0;
			gbc1.gridwidth = 2;
			gbc1.insets = new Insets(0, 0, 5, 0);
			p1.add(l1, gbc1);
			
			JLabel l2 = new JLabel("in Object Program: ");
			gbc1.gridx = 0;
			gbc1.gridy = 1;
			gbc1.gridwidth = 1;
			gbc1.insets = new Insets(0, 0, 5, 5);
			p1.add(l2, gbc1);
			
			END_ADDR = new JLabel("");
			END_ADDR.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			gbc1.gridx = 1;
			gbc1.gridy = 1;
			gbc1.gridwidth = 1;
			gbc1.insets = new Insets(0, 0, 5, 0);
			p1.add(END_ADDR, gbc1);
			
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			this.add(p1, gbc);
			
			//Labels
			JPanel p2 = new JPanel();
			p2.setLayout(new GridBagLayout());
			p2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.fill = GridBagConstraints.BOTH;
			
			JLabel l3 = new JLabel("Start Address in Memory");
			gbc2.gridx = 0;
			gbc2.gridy = 0;
			gbc2.gridwidth = GridBagConstraints.REMAINDER;
			gbc2.insets = new Insets(0, 0, 5, 0);
			p2.add(l3, gbc2);
			
			JLabel l4 = new JLabel(" ");
			gbc2.gridx = 0;
			gbc2.gridy = 1;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 0);
			p2.add(l4, gbc2);
			
			StartAddrMem = new JLabel("");
			StartAddrMem.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			gbc2.gridx = 1;
			gbc2.gridy = 1;
			gbc2.weightx = 0.5;
			gbc2.weighty = 0.5;
			gbc2.insets = new Insets(0, 0, 5, 0);
			p2.add(StartAddrMem, gbc2);
			
			JLabel l5 = new JLabel("Target Address: ");
			gbc2.gridx = 0;
			gbc2.gridy = 2;
			gbc2.weightx = 0;
			gbc2.weighty = 0;
			gbc2.gridwidth = 1;
			gbc2.insets = new Insets(0, 0, 5, 5);
			p2.add(l5, gbc2);
			
			TA = new JLabel("");
			TA.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			gbc2.gridx = 1;
			gbc2.gridy = 2;
			gbc2.gridwidth = GridBagConstraints.REMAINDER;
			gbc2.insets = new Insets(0, 0, 5, 0);
			p2.add(TA, gbc2);
			
			JLabel l6 = new JLabel("Instructions :");
			gbc2.gridx = 0;
			gbc2.gridy = 3;
			gbc2.gridwidth = GridBagConstraints.REMAINDER;
			gbc2.insets = new Insets(0, 0, 5, 0);
			p2.add(l6, gbc2);
			
			gbc.gridx = 0;
			gbc.gridy = 1;
			this.add(p2, gbc);
			
			//Instructions panel
			
			JPanel p3 = new JPanel();
			model = new DefaultListModel();
			Insts = new JList(model);
			//Insts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane = new JScrollPane(Insts);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
			scrollPane.setPreferredSize(new Dimension(120, 220));
			p3.add(scrollPane);
			
			JPanel p4 = new JPanel();
			p4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			p4.setLayout(new GridBagLayout());
			GridBagConstraints gbc3 = new GridBagConstraints();
			gbc3.fill = GridBagConstraints.BOTH;
			
			JLabel l7 = new JLabel("사용중인 장치",JLabel.CENTER);
			gbc3.gridy = 0;
			gbc3.gridwidth = GridBagConstraints.REMAINDER;
			p4.add(l7, gbc3);
			
			device = new JLabel(" ");
			device.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			gbc3.gridy = 1;
			gbc3.insets = new Insets(0, 0, 90,0);
			p4.add(device, gbc3);
			
			ex_step = new JButton("실행(1step)");
			gbc3.gridy = 2;
			gbc3.insets = new Insets(0, 0, 10 ,0);
			p4.add(ex_step, gbc3);
			ex_all = new JButton("실행 (all)");
			gbc3.gridy = 3;
			p4.add(ex_all, gbc3);
			exit = new JButton("종료");
			gbc3.gridy = 4;
			p4.add(exit, gbc3);
			p3.add(p4);
			
			gbc.gridx = 0;
			gbc.gridy = 2;
			this.add(p3, gbc);

		}
	}
}
