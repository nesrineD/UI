package flink.gelly;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JTextField;
import java.awt.Button;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.SystemColor;
import javax.swing.JButton;

public class gui extends JFrame {

	private JPanel contentPane;
	private JTextField sent;
	private JTextField impl;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui frame = new gui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public gui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 557, 235);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblSentence = new JLabel("Sentence");
		lblSentence.setForeground(Color.BLUE);
		lblSentence.setBounds(27, 32, 58, 14);
		contentPane.add(lblSentence);
		
		sent = new JTextField();
		lblSentence.setLabelFor(sent);
		sent.setBounds(208, 26, 323, 26);
		contentPane.add(sent);
		sent.setColumns(10);
		
		JLabel lblWhatIsThis = new JLabel("What is this sentence about ?");
		lblWhatIsThis.setForeground(Color.BLUE);
		lblWhatIsThis.setBounds(27, 82, 171, 14);
		contentPane.add(lblWhatIsThis);
		
		impl = new JTextField();
		lblWhatIsThis.setLabelFor(impl);
		impl.setBounds(208, 76, 323, 26);
		contentPane.add(impl);
		impl.setColumns(10);
		
		Button button = new Button("Construct Graph");
		button.setForeground(Color.BLUE);
		button.setFont(new Font("Dialog", Font.BOLD, 12));
		button.setBackground(SystemColor.controlHighlight);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		button.setBounds(27, 142, 99, 20);
		contentPane.add(button);
		
		Button button_1 = new Button("New Sentence");
		button_1.setForeground(Color.BLUE);
		button_1.setFont(new Font("Dialog", Font.BOLD, 12));
		button_1.setBackground(SystemColor.controlHighlight);
		button_1.setBounds(148, 142, 99, 20);
		contentPane.add(button_1);
		
		Button button_2 = new Button("Clustering");
		button_2.setForeground(Color.BLUE);
		button_2.setFont(new Font("Dialog", Font.BOLD, 12));
		button_2.setBackground(SystemColor.controlHighlight);
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		button_2.setBounds(403, 142, 99, 20);
		contentPane.add(button_2);
		
		Button button_3 = new Button("Visualize Graph");
		button_3.setForeground(Color.BLUE);
		button_3.setFont(new Font("Dialog", Font.BOLD, 12));
		button_3.setBackground(SystemColor.controlHighlight);
		button_3.setBounds(275, 142, 99, 20);
		contentPane.add(button_3);
		
		Button button_4 = new Button("Visualize Graph");
		button_4.setForeground(Color.BLUE);
		button_4.setFont(new Font("Dialog", Font.BOLD, 12));
		button_4.setBackground(SystemColor.controlHighlight);
		button_4.setBounds(27, 142, 99, 20);
		contentPane.add(button_4);
	}
}
