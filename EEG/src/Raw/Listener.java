package Raw;

import java.awt.*;
import java.awt.event.*;

public class Listener extends Frame implements KeyListener {

	private static final long serialVersionUID = 1L;
	TextField t1;
	Label l1;
	Label space;
	Label l2;
	Label space2;
	Label l3;
	
	public Listener (String s) {
		super(s);
		Panel p = new Panel();
		p.setLayout(new GridLayout(6,3));
		// couldn't set size for some reason
		l1 = new Label("Waiting for signals to stabilize...",Label.CENTER);
		space = new Label ("",Label.CENTER);
		l2 = new Label("",Label.CENTER);
		space2 = new Label ("",Label.CENTER);
		l3 = new Label("",Label.CENTER);
		l1.setFont(new Font("SansSerif", Font.BOLD, 13));
		l2.setFont(new Font("SansSerif", Font.PLAIN, 13));
		l3.setFont(new Font("SansSerif", Font.PLAIN+Font.ITALIC, 13));
		//l1.setLocation(20, 5);
		//l2.setLocation(20, 20);
		p.add(l1);
		p.add(space);
		p.add(l2);
		p.add(space2);
		p.add(l3);
		add(p);
		addKeyListener(this);
		setSize(500,100);
		setVisible(true);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				RawData.cleanUp();
			}
		});
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() != KeyEvent.VK_SPACE) return;
		
		RawData.keyPressed = !RawData.keyPressed;
		if(RawData.keyPressed) {
			l1.setText("Recording pattern...");
			l3.setText("");
		} else {
			l1.setText("Idle");
		}
		
	}

	public void setLabel(String str) {
		l1.setText(str);
		l2.setText("");
		l3.setText("");
	}
	
	public void setLabel(String str, String str2) {
		l1.setText(str);
		l2.setText(str2);
		l3.setText("");
	}
	
	public void setLabel(String str, String str2, String str3) {
		l1.setText(str);
		l2.setText(str2);
		l3.setText(str3);
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		//l1.setText("Key Released");
		//RawData.keyPressed = false;
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		//l1.setText("Key Typed");
		
	}

}
