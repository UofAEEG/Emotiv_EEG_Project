package Visualization;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;


public class Listener extends Frame implements KeyListener {

	private static final long serialVersionUID = 1L;
	TextField t1;
	Label l1;
	
	public Listener (String s) {
		super(s);
		Panel p = new Panel();
		l1 = new Label("EEG Key Listener");
		p.add(l1);
		add(p);
		addKeyListener(this);
		setSize(200,100);
		setVisible(true);

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				Data.collecting = false;
				
			}
		});
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		Data.keyPressed = !Data.keyPressed;
		if(Data.keyPressed) {
			l1.setText("Recording...");
		} else {
			l1.setText("Idle");
		}
		
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
