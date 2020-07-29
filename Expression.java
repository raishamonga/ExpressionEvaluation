package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	String exprTrimmed = expr.trim().replaceAll("\\s","");
    	
    	for (int i =0; i < exprTrimmed.length(); i++) {
    		char c = exprTrimmed.charAt(i);
    		if (Character.isLetter(c)) {
    			for (int j=i; j < exprTrimmed.length(); j++) {
    				char d = exprTrimmed.charAt(j);
    				if (!Character.isLetter(d)) {
						if(d == '[') {
							Array newArray = new Array(exprTrimmed.substring(i,j));
							arrays.add(newArray);
						} else {
							Variable newVariable = new Variable(exprTrimmed.substring(i,j));
							vars.add(newVariable);
						}
						i = j;
						break;
    				}

    				if (j == exprTrimmed.length() -1) {
    					Variable newVariable = new Variable(exprTrimmed.substring(i,j+1));
						vars.add(newVariable);
						i = j;
						break;
    				}

    			}
    		}
    	}
    	
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	  //ev) {aluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays
    	    	int i = 0;
    	    	expr.trim();
    	        Stack<Character> stacker = new Stack<>();
    	        Stack<Float> stackend = new Stack<>();
    	        Stack<String> stackar = new Stack<>();

    	    	StringBuffer piecess = new StringBuffer("");
    			float nValue = 0;
    	    	while (i < expr.length()) {
    	    		switch(expr.charAt(i)) {
    	    			case '(':
    	    				stacker.push(expr.charAt(i));
    	    				break;
    	    			case ')':
    						while(!stacker.isEmpty() && !stackend.isEmpty() && (stacker.peek() != '(')) {   				
    							calculate(stacker, stackend);
    						}
    	    				if(stacker.peek() == '(') {
    	    					stacker.pop();
    	    				}
    	    			break;
    	    			case '[':
    	    				stackar.push(piecess.toString());
    			            piecess.setLength(0);    							    					
    			            stacker.push(expr.charAt(i));
    	    			break;
    	    			case ']':
    						while(!stacker.isEmpty() && !stackend.isEmpty() && (stacker.peek() != '[')) {   				
    							calculate(stacker, stackend);
    						}
    	    				if(stacker.peek() == '[') {
    	    					stacker.pop();
    	    				}
    	    				int nIndex = stackend.pop().intValue();
    	     				Iterator<Array> itr = arrays.iterator();
    	     				while (itr.hasNext()) {
    	     					Array arr = itr.next();
    	     					if(arr.name.equals(stackar.peek())) {
    	     						stackend.push((float) arr.values[nIndex]);
    	     						stackar.pop();
    	         				    break;
    	     					}
    	     				} 
    	    			break;
    	    			case ' ':
    	    				break;
    	    			case '+':
    	     			case '-':
    	    			case '*':
    	    			case '/':
    	    				while(!stacker.isEmpty() && (stacker.peek() != '(') && (stacker.peek() != '[') && isLowerPrecedence(expr.charAt(i), stacker.peek())){
    							calculate(stacker, stackend);						
    						}
    	    				stacker.push(expr.charAt(i));    						
    	    				break;
    	    			default:
    		    			if((expr.charAt(i) >= 'a' &&  expr.charAt(i) <= 'z') || (expr.charAt(i) >= 'A' &&  expr.charAt(i) <= 'Z')) {
    		    				piecess.append(expr.charAt(i));
    		    				if(i + 1  < expr.length()) {
    		    					/*
    		    					if(expr.charAt(i +1) == '[') {
    		    						// variable is an array
    		    						
    		    					}
    		    					else */ if (expr.charAt(i +1) == '+' ||
    		    							 expr.charAt(i +1) == '-' ||
    		    							 expr.charAt(i +1) == '*' ||
    		    							 expr.charAt(i +1) == '/' ||
    		    							 expr.charAt(i +1) == ')' ||
    		    							 expr.charAt(i +1) == ']' ||
    		    							 expr.charAt(i +1) == ' '){
    			    		            Variable var = new Variable(piecess.toString());
    			    		            int varIndex = vars.indexOf(var);
    			    		            nValue = vars.get(varIndex).value;
    				    				
    			    		            stackend.push(nValue);
    			    		            piecess.setLength(0);
    		    						
    		    					} 
    		    					
    		    				}
    		    				else {
    		    		            Variable var = new Variable(piecess.toString());
    		    		            int varIn = vars.indexOf(var);
    		    		            nValue = vars.get(varIn).value;
    		    		            stackend.push(nValue);
    		    		            piecess.setLength(0);    							    					
    		    				}
    		    				
    		    				
    		    			}
    		    			else if(expr.charAt(i) >= '0' &&  expr.charAt(i) <= '9') {
    		    				piecess.append(expr.charAt(i));
    		    				if(i + 1  < expr.length()) {
    		    					if ( expr.charAt(i +1) == '+' ||
    									 expr.charAt(i +1) == '-' ||
    									 expr.charAt(i +1) == '*' ||
    									 expr.charAt(i +1) == '/' ||
    									 expr.charAt(i +1) == ')' ||
    									 expr.charAt(i +1) == ']' ||
    									 expr.charAt(i +1) == ' '){
    			    					nValue = Integer.parseInt(piecess.toString());
    			    					stackend.push(nValue);
    			    					piecess.setLength(0);
    		    						
    		    					} 
    		    					
    		    				}
    		    				else {
    		    					nValue = Float.parseFloat(piecess.toString());
    		    					stackend.push(nValue);
    		    					piecess.setLength(0);
    		    				}

    		    			}	    				
    		    			break;
    	    		}
    	    		i++;
    	    		
    	    	}
    	    	Float nResult = Float.valueOf(0);    	
    			if(i == expr.length()) {
    				while(stacker.size() > 0 &&  stackend.size() > 1) {
    					calculate(stacker, stackend);
    				}
    				if(stackend.size() > 0) {
    					nResult = stackend.pop();
    				}
    				
    			}

    	    	return nResult.floatValue();
    	    }
    	    private static boolean 
    	    isLowerPrecedence(char charFir, char charSec) {
    	    	if((charFir == '*' || charFir == '/') && (charSec == '+' || charSec == '-')){
    	    		return false;
    	    	}
    	    	return true;
    	    }
    	    private static void calculate(Stack<Character> stacker, Stack<Float> stackend) {
    			Float nResult = Float.valueOf(0);		
    			if(stacker.size() > 0 &&  stackend.size() > 1) {
    		    	Float nVar1 = stackend.pop().floatValue();
    				Float nVar2 = stackend.pop().floatValue();
    				switch(stacker.pop()) {
    				case '+':
    					nResult = nVar2 + nVar1;
    					break;
    				case '-':
    					nResult = nVar2 - nVar1;
    					break;
    				case '*':
    					nResult = nVar2 * nVar1;
    					break;
    				case '/':
    					nResult = nVar2 / nVar1;
    					break;
    				}
    				stackend.push(nResult);
    			}
    			else if(stackend.size() > 0){
    		    	nResult = stackend.pop();
    		    	stackend.push(stackend.pop().floatValue());
    				
    			}

    	    }
    	    	
    	}