package juttu.jsconsole;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Stack;

import javax.script.*;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;

public class JSConsole implements CaretListener{

	protected class KeyEnterAction implements Action{

		JSConsole console;
		
		public KeyEnterAction(JSConsole console){
			this.console = console;
		}
		
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			int len = editorPane.getDocument().getLength();
			try {
				editorPane.getDocument().insertString(len, "\n", null);
				if (len - startPosition < 1) {
					console.newPrompt();
					return;
				}

				String command = editorPane.getDocument().getText(
						startPosition, len - startPosition);

				console.runCommand(command);

			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}

		public Object getValue(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public void putValue(String key, Object value) {
			// TODO Auto-generated method stub
			
		}

		public void setEnabled(boolean b) {
			// TODO Auto-generated method stub
			
		}

		public boolean isEnabled() {
			// TODO Auto-generated method stub
			return true;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected class KeyDownAction implements Action{

		public void actionPerformed(ActionEvent e) {
			
			if(stackPosition == history.size()){ //latest command, nothing after
				return;
			}
			
			stackPosition++;
			
			String command =  stackPosition < history.size() ? history.get(stackPosition) : typedCommand ;
	
			// restore the command
			try {
				//remove the current command line
				editorPane.getDocument().remove(startPosition, editorPane.getDocument().getLength() - startPosition);
				editorPane.getDocument().insertString(startPosition, command, defaultStyle);
				editorPane.setCaretPosition(editorPane.getDocument().getLength());
			} catch (BadLocationException ex) {
				System.out.println(ex);
			}
			
		}

		public Object getValue(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public void putValue(String key, Object value) {
			// TODO Auto-generated method stub
			
		}

		public void setEnabled(boolean b) {
			// TODO Auto-generated method stub
			
		}

		public boolean isEnabled() {
			// TODO Auto-generated method stub
			return true;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected class KeyUpAction implements Action{

		
		public void actionPerformed(ActionEvent e) {
			
			//going up the history from new command
			if( stackPosition == history.size() ){ //save the current input
				int len = editorPane.getDocument().getLength();
				try {
					if ( len > startPosition) {
						typedCommand = editorPane.getDocument().getText(
								startPosition, len - startPosition);

					}
				} catch (BadLocationException ex) {
					System.out.println(ex);
				}
			}
			
			//beginning of history reached
			if( stackPosition == 0){
				return; 
			}
			
			String command = history.get(--stackPosition);
			
			// restore the command
			try {
				//remove the current command line
				editorPane.getDocument().remove(startPosition, editorPane.getDocument().getLength() - startPosition);
				editorPane.getDocument().insertString(startPosition, command, defaultStyle);
				editorPane.setCaretPosition(editorPane.getDocument().getLength());
			} catch (BadLocationException ex) {
				System.out.println(ex);
			}
		}

		public Object getValue(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public void putValue(String key, Object value) {
			// TODO Auto-generated method stub
			
		}

		public void setEnabled(boolean b) {
			// TODO Auto-generated method stub
			
		}

		public boolean isEnabled() {
			// TODO Auto-generated method stub
			return true;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	protected class BackAction implements Action{

		
		public void actionPerformed(ActionEvent e) {
			//only delete characters if the caret is after the prompt start
			if( editorPane.getCaretPosition() > startPosition ){
				backAction.actionPerformed(e);
			}
		
		}

		public Object getValue(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public void putValue(String key, Object value) {
			// TODO Auto-generated method stub
			
		}

		public void setEnabled(boolean b) {
			// TODO Auto-generated method stub
			
		}

		public boolean isEnabled() {
			// TODO Auto-generated method stub
			return true;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	
	
	//The JS engine
	ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
	
	//Writer to get output from the JS engine
	StringWriter normalOutput;
	
	//Bindings bindings = newContext.getBindings(ScriptContext.ENGINE_SCOPE);// = new SimpleBindings();
	
	//The view element of the console
	JFrame consoleFrame;
	JEditorPane editorPane;
	
	//Commands history stack
	Stack<String> history;
	String typedCommand;
	int stackPosition = -1;
	
	//Text styles
	AttributeSet defaultStyle;
	AttributeSet chevronStyle;
	AttributeSet outputStyle;
	AttributeSet errorStyle;
	
	//Custom key input action
	Action backAction;
	int startPosition;
	

	
	public JSConsole(boolean showConsole){
		
		//Initialise the writers to get output from the JS engine
		ScriptContext newContext = new SimpleScriptContext();

		normalOutput = new StringWriter();
		newContext.setWriter(normalOutput);
		
		newContext.setErrorWriter(normalOutput);
		engine.setContext(newContext);
		
		//Initialise history
		history = new Stack<String>();
		
		//Create the console window
		consoleFrame = new JFrame("Mimodek Console");
		consoleFrame.setSize(400, 300);
		
		//Create the editor pane and register custom actions on inputs
		editorPane = new JEditorPane();
		
		//Prevent the deletion of the prompt start indicator
		// Save the default action
		backAction = editorPane.getActionMap().get( editorPane.getInputMap().get(KeyStroke.getKeyStroke("BACK_SPACE") ) );
		editorPane.getActionMap().put("key-back", new BackAction() );
		editorPane.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), "key-back");
		
		//Show previous command in history
		editorPane.getActionMap().put("key-up", new KeyUpAction() );
		editorPane.getInputMap().put(KeyStroke.getKeyStroke("UP"), "key-up");
		
		//Show next command in history
		editorPane.getActionMap().put("key-down", new KeyDownAction() );
		editorPane.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "key-down");
		
		//Execute command
		editorPane.getActionMap().put("key-enter", new KeyEnterAction(this) );
		editorPane.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "key-enter");

		
	
		//Prevent the caret from going behind the prompt start indicator ">"
		editorPane.addCaretListener(this);
		
		//enable Rich Text Format
		editorPane.setEditorKit( editorPane.getEditorKitForContentType("text/rtf") );

		//Output Text Styles
		StyleContext styleContext = StyleContext.getDefaultStyleContext();
		
		defaultStyle = styleContext.getEmptySet();
		defaultStyle = styleContext.addAttribute(defaultStyle, StyleConstants.Foreground, new Color(0,0,0));
		
		chevronStyle = styleContext.getEmptySet();
		chevronStyle = styleContext.addAttribute(chevronStyle, StyleConstants.Foreground, new Color(0,0,255));
		
		outputStyle = styleContext.getEmptySet();
		outputStyle = styleContext.addAttribute(outputStyle, StyleConstants.Foreground, new Color(100,100,100));
		outputStyle = styleContext.addAttribute(outputStyle, StyleConstants.Italic, true);
		
		errorStyle = styleContext.getEmptySet();
		errorStyle = styleContext.addAttribute(errorStyle, StyleConstants.Foreground, new Color(255,0,0));
		errorStyle = styleContext.addAttribute(errorStyle, StyleConstants.Italic, true);
		
		
		//Put the editor pane in a scroll pane
		JScrollPane scrollPane = new JScrollPane(editorPane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		consoleFrame.add(scrollPane);
		
		consoleFrame.setVisible(showConsole);
		
		bindToJs("console", this);
		
		//Init a new command prompt
		newPrompt();
	}
	
	public JSConsole(String pathToBootFile, boolean showConsole) throws FileNotFoundException{
		this( showConsole );
		loadJSFile(pathToBootFile);
	}
	
	public void loadJSFile(String pathToJSFile) throws FileNotFoundException{
		BufferedReader in  = new BufferedReader(new FileReader( pathToJSFile ));
		try {
			engine.eval(in);
		} catch (ScriptException e) {
			errorOutput( e.getMessage() );
		} finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void openConsole(){
		if( !consoleFrame.isVisible() ){
			consoleFrame.setVisible(true);
		}
		consoleFrame.toFront();
	}
	
	public void closeConsole() {
		consoleFrame.setVisible(false);
	}
	
	public String getCommand(){
		int len = editorPane.getDocument().getLength();
		try {
			return editorPane.getDocument().getText(startPosition, len - startPosition);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	public void runCommand(String command) {
		runCommand(command, false);
	}
	
	public void runCommand(String command, boolean silent) {
		//Execute the command silently and the command won't be added to the history
		if( silent ){
			try{
				engine.eval( command );
			} catch (ScriptException e) {
				System.err.println(e);
			}
			return;
		}
		
		//push the command on the history stack
		history.push(command);
		
		String result;
		
		//Evaluate the JavaScript code
		try {
			
			Object returnedVal = engine.eval( command );
			result = new String(normalOutput.getBuffer());
			normalOutput.flush();
			normalOutput.getBuffer().setLength(0);
			//Output the result
			if( result != null){
				evalOutput( result );
			}
			if( returnedVal != null ){
				evalOutput( returnedVal.toString()+"\n" );
			}
		} catch (ScriptException e) {
			errorOutput( e.getMessage() );
		}
		newPrompt();
	}
	
	private void errorOutput(String error) {
		try {
			int len = editorPane.getDocument().getLength();
			editorPane.getDocument().insertString(len, error+"\n", errorStyle);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void evalOutput(String output){
		try {
			int len = editorPane.getDocument().getLength();
			editorPane.getDocument().insertString(len, output, outputStyle);

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void newPrompt(){
		try {
			int len = editorPane.getDocument().getLength();
			editorPane.getDocument().insertString(len, " >", chevronStyle);
			editorPane.getDocument().insertString(len+2, " ", defaultStyle);
			startPosition = len+3;
			typedCommand = "";
			stackPosition = history.size();
			
			editorPane.setCaretPosition(startPosition);
			//editorPane.getDocument().
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void bindToJs(String name, Object obj){
		engine.put(name, obj);
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println(e.getKeyChar()+" "+e.getKeyCode());
		
	}

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void caretUpdate(CaretEvent e) {
		// TODO Auto-generated method stub
		if( e.getDot() < startPosition ){
			editorPane.setCaretPosition( startPosition );
		}
		
	}


	
	/*
	public Object getJsVal(String name){
		return newContext.getAttribute(name);
	}
	*/

}
