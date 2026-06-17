package org.hesham.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hesham.lox.TokenType.AND;
import static org.hesham.lox.TokenType.BANG;
import static org.hesham.lox.TokenType.BANG_EQUAL;
import static org.hesham.lox.TokenType.CLASS;
import static org.hesham.lox.TokenType.COMMA;
import static org.hesham.lox.TokenType.DOT;
import static org.hesham.lox.TokenType.ELSE;
import static org.hesham.lox.TokenType.EOF;
import static org.hesham.lox.TokenType.EQUAL;
import static org.hesham.lox.TokenType.EQUAL_EQUAL;
import static org.hesham.lox.TokenType.FALSE;
import static org.hesham.lox.TokenType.FOR;
import static org.hesham.lox.TokenType.FUN;
import static org.hesham.lox.TokenType.GREATER;
import static org.hesham.lox.TokenType.GREATER_EQUAL;
import static org.hesham.lox.TokenType.IDENTIFIER;
import static org.hesham.lox.TokenType.IF;
import static org.hesham.lox.TokenType.LEFT_BRACE;
import static org.hesham.lox.TokenType.LEFT_PAREN;
import static org.hesham.lox.TokenType.LESS;
import static org.hesham.lox.TokenType.LESS_EQUAL;
import static org.hesham.lox.TokenType.MINUS;
import static org.hesham.lox.TokenType.NIL;
import static org.hesham.lox.TokenType.NUMBER;
import static org.hesham.lox.TokenType.OR;
import static org.hesham.lox.TokenType.PLUS;
import static org.hesham.lox.TokenType.PRINT;
import static org.hesham.lox.TokenType.RETURN;
import static org.hesham.lox.TokenType.RIGHT_BRACE;
import static org.hesham.lox.TokenType.RIGHT_PAREN;
import static org.hesham.lox.TokenType.SEMICOLON;
import static org.hesham.lox.TokenType.SLASH;
import static org.hesham.lox.TokenType.STAR;
import static org.hesham.lox.TokenType.STRING;
import static org.hesham.lox.TokenType.SUPER;
import static org.hesham.lox.TokenType.THIS;
import static org.hesham.lox.TokenType.TRUE;
import static org.hesham.lox.TokenType.VAR;
import static org.hesham.lox.TokenType.WHILE;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 0;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }
    
    List<Token> scanTokens(){
        while(!isAtEnd()){
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break; 
            case '!': 
                addToken(match('=') ? BANG_EQUAL : BANG); 
                break;
            case '=': 
                addToken(match('=') ? EQUAL_EQUAL : EQUAL); 
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if(match('/')){
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    while(peek() != '*' && peekNext() != '/' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                    break;
                }
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n': line++; break;
            case '"': string(); break;
            default:
                if(isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                }else {
                    Lox.error(line, "Unexpected character!");
                }
                break;         
        }
    }

    private boolean match(char expected){
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance(){
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }
        if(isAtEnd()){
            Lox.error(line, "Undetermined String.");
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isAlpha(char c){
        return  (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                 c == '_';
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }
}