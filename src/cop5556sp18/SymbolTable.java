package cop5556sp18;

import cop5556sp18.AST.Declaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

public class SymbolTable {

    private final HashMap<String, LinkedHashMap<Integer, Declaration>> symbolTable = new HashMap<>();
    // scope with serial numbers
    private Stack<Integer> scopeStack = new Stack<>();
    private int currentScope;

    public SymbolTable() {
        currentScope = 0;
        scopeStack.push(0);
    }

    public void enterScope() {
        // scopeCounter = scopeCounter++;
        scopeStack.push(++currentScope);
    }

    public void closeScope() {
        scopeStack.pop();
    }

    public int getScope() {
        return scopeStack.peek();
    }

    /**
     * Gets matching entry in hash table. Scan the chain and return attributes for
     * entry with scope number closest to the top of the scope
     *
     * @param identifier
     * @return Declaration or null
     */
    public Declaration lookup(String identifier) {
        ArrayList<Integer> scopeArray = new ArrayList<>(scopeStack);
        LinkedHashMap<Integer, Declaration> hashTableChain = symbolTable.get(identifier);
        if (hashTableChain == null) {
            return null;
        }
        for (int i = scopeArray.size() - 1; i >= 0; i--) {
            int serialNum = scopeArray.get(i);
            if (hashTableChain.get(serialNum) != null) {
                return hashTableChain.get(serialNum);
            }
        }
        return null;
    }

    /**
     * Insert new entry for current scope. If the hashTableChain is null, create
     * new chain. If the chain doesn't have the current scope, put the scope in
     * the chain along with the attributes, and then add it to the symbolTable map.
     *
     * @param identifier
     * @param declaration
     * @return
     */
    public boolean insert(String identifier, Declaration declaration) {
        int currentScope = getScope();
        LinkedHashMap<Integer, Declaration> hashTableChain = symbolTable.get(identifier);
        if (hashTableChain == null) {
            hashTableChain = new LinkedHashMap<>();
        }
        if (hashTableChain.get(currentScope) == null) {
            hashTableChain.put(currentScope, declaration);
            symbolTable.put(identifier, hashTableChain);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String result = "";
        for (String identifier : symbolTable.keySet()) {
            result += String.format("Variable or Name: %s \n", identifier);
            LinkedHashMap<Integer, Declaration> hashTableChain = symbolTable.get(identifier);
            for (Integer scope : hashTableChain.keySet()) {
                result += String.format("\t Scope number and attributes: %s, %s \n", scope, hashTableChain.get(scope));
            }
        }
        return result;
    }

}
