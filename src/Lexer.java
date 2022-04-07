/*
 * File: Lexer.java
 * Date: Spring 2022
 * Auth: Cameron S. Williamson
 * Desc: A lexer for MyPL.
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;


public class Lexer {

  private BufferedReader buffer; // handle to input stream
  private int line = 1;          // current line number
  private int column = 0;        // current column number


  //--------------------------------------------------------------------
  // Constructor
  //--------------------------------------------------------------------
  
  public Lexer(InputStream instream) {
    buffer = new BufferedReader(new InputStreamReader(instream));
  }


  //--------------------------------------------------------------------
  // Private helper methods
  //--------------------------------------------------------------------

  // Returns next character in the stream. Returns -1 if end of file.
  private int read() throws MyPLException {
    try {
      return buffer.read();
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return -1;
  }

  
  // Returns next character without removing it from the stream.
  private int peek() throws MyPLException {
    int ch = -1;
    try {
      buffer.mark(1);
      ch = read();
      buffer.reset();
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return ch;
  }


  // Print an error message and exit the program.
  private void error(String msg, int line, int column) throws MyPLException {
    msg = msg + " at line " + line + ", column " + column;
    throw MyPLException.LexerError(msg);
  }

  
  // Checks for whitespace 
  public static boolean isWhitespace(int ch) {
    return Character.isWhitespace((char)ch);
  }

  
  // Checks for digit
  private static boolean isDigit(int ch) {
    return Character.isDigit((char)ch);
  }

  
  // Checks for letter
  private static boolean isLetter(int ch) {
    return Character.isLetter((char)ch);
  }

  
  // Checks if given symbol
  private static boolean isSymbol(int ch, char symbol) {
    return (char)ch == symbol;
  }

  
  // Checks if end-of-file
  private static boolean isEOF(int ch) {
    return ch == -1;
  }
  

  //--------------------------------------------------------------------
  // Public next_token function
  //--------------------------------------------------------------------
  
  // Returns next token in input stream
  public Token nextToken() throws MyPLException {
    // Whitespace, comments, and division
    while (isWhitespace(peek()) || isSymbol(peek(), '#')) {
      if (peek() == '\n') {
        line++;
        column = -1;
      }
      if (peek() == '#') {
        read();
        column++;
        while (peek() != '\n' && !isEOF(peek())) {
          read();
          column++;
        }
      }
      read();
      column++;
    }

    if (isEOF(peek()))
      return new Token(TokenType.EOS, "EOF", line, ++column);

    if (isSymbol(peek(), ',')) {
      read();
      column++;
      return new Token(TokenType.COMMA, ",", line, column);
    }
    if (isSymbol(peek(), '/')) {
      read();
      column++;
      return new Token(TokenType.DIVIDE, "/", line, column);
    }
    if (isSymbol(peek(), '.')) {
      read();
      column++;
      return new Token(TokenType.DOT, ".", line, column);
    }
    if (isSymbol(peek(), '+')) {
      read();
      column++;
      return new Token(TokenType.PLUS, "+", line, column);
    }
    if (isSymbol(peek(), '-')) {
      read();
      column++;
      return new Token(TokenType.MINUS, "-", line, column);
    }
    if (isSymbol(peek(), '*')) {
      read();
      column++;
      return new Token(TokenType.MULTIPLY, "*", line, column);
    }
    if (isSymbol(peek(), '%')) {
      read();
      column++;
      return new Token(TokenType.MODULO, "%", line, column);
    }
    if (isSymbol(peek(), '{')) {
      read();
      column++;
      return new Token(TokenType.LBRACE, "{", line, column);
    }
    if (isSymbol(peek(), '}')) {
      read();
      column++;
      return new Token(TokenType.RBRACE, "}", line, column);
    }
    if (isSymbol(peek(), '(')) {
      read();
      column++;
      return new Token(TokenType.LPAREN, "(", line, column);
    }
    if (isSymbol(peek(), ')')) {
      read();
      column++;
      return new Token(TokenType.RPAREN, ")", line, column);
    }
    if (isSymbol(peek(), '=')) {
      read();
      column++;
      if (isSymbol(peek(), '=')) {
        read();
        return new Token(TokenType.EQUAL, "==", line, column++);
      }
      return new Token(TokenType.ASSIGN, "=", line, column);
    }
    if (isSymbol(peek(), '<')) {
      read();
      column++;
      if (isSymbol(peek(), '=')) {
        read();
        return new Token(TokenType.LESS_THAN_EQUAL, "<=", line, column++);
      }
      return new Token(TokenType.LESS_THAN, "<", line, column);
    }
    if (isSymbol(peek(), '>')) {
      read();
      column++;
      if (isSymbol(peek(), '=')) {
        read();
        return new Token(TokenType.GREATER_THAN_EQUAL, ">=", line, column++);
      }
      return new Token(TokenType.GREATER_THAN, ">", line, column);
    }
    if (isSymbol(peek(), '!')) {
      read();
      column++;
      if (isSymbol(peek(), '=')) {
        read();
        return new Token(TokenType.NOT_EQUAL, "!=", line, column++);
      }
      if (isSymbol(peek(), '>')) {
        error("expecting '=', found '>'", line, ++column);
      }
    }

    // Characters
    if (isSymbol(peek(), '\'')) {
      read();
      column++;
      int initialColumn = column;
      String lexeme = "";
      while (!isSymbol(peek(), '\'') && !isEOF(peek())) {
        lexeme += (char) read();
        column++;
      }
      if (lexeme.length() > 1 && !lexeme.contains("\\")) {
        error("expecting ' found, '" + lexeme.charAt(lexeme.length() - 1) + "'", line, ++initialColumn);
      } else if (lexeme.length() == 0) {
        error("empty character", line, initialColumn);
      } else if (lexeme.contains("\n")) {
        error("found newline in character", line, column);
      } 
      read();
      column++;
      return new Token(TokenType.CHAR_VAL, lexeme, line, initialColumn);
    }
    // if (isSymbol(peek(), '\'')) {
    //   read();
    //   column++;
    //   int initialColumn = column;
    //   int ch = read();
    //   column++;
    //   if (isSymbol(ch, '\'')) {
    //     error("empty character", line, initialColumn);
    //   } else if (isSymbol(ch, '\\')) {
    //     ch = read();
    //     column++;
    //     read();
    //     column++;
    //     return new Token(TokenType.CHAR_VAL, "\\" + String.valueOf((char) ch), line, initialColumn);
    //   }
    //   char thirdChar = (char) read();
    //   column++;
    //   if (!isSymbol(thirdChar, '\'')) {
    //     error("expecting ' found, '" + thirdChar + "'", line, ++initialColumn);
    //   }
    //   return new Token(TokenType.CHAR_VAL, String.valueOf((char) ch), line, initialColumn);
    // }

    // Strings
    if (isSymbol(peek(), '"')) {
      read();
      column++;
      int initialColumn = column;
      StringBuilder sb = new StringBuilder();
      while (peek() != '"') {
        if (isSymbol(peek(), '\n')) {
          error("found newline within string", line, ++column);
        } else if (isEOF(peek())) {
          error("found end-of-file in string", line, column);
        }
        sb.append((char) read());
        column++;
      }
      read();
      column++;
      return new Token(TokenType.STRING_VAL, sb.toString(), line, initialColumn);
    }

    // Int / Doubl
    if (isDigit(peek())) {
      StringBuilder sb = new StringBuilder();
      int initialColumn = column + 1;
      Boolean isDouble = false;
      while (isDigit(peek()) || isSymbol(peek(), '.')) {
        sb.append((char) read());
        column++;
        if (isLetter(peek())) {
          error("missing decimal digit in double value '" + sb.toString() + "'", line, initialColumn);
        }
        if (isSymbol(peek(), '.')) {
          if (isDouble) {
            error("too many decimal points in double value '" + sb.toString() + "'", line, initialColumn);
          }
          isDouble = true;
        }
      }
      if (sb.toString().charAt(0) == '0' && sb.toString().length() > 1 && sb.toString().charAt(1) != '.') {
        error("leading zero in '" + sb.toString() + "'", line, initialColumn);
      }
      if (sb.toString().contains(".")) {
        return new Token(TokenType.DOUBLE_VAL, sb.toString(), line, initialColumn);
      }
      return new Token(TokenType.INT_VAL, sb.toString(), line, initialColumn);
    }

    // Keywords and Identifiers
    if (isLetter(peek())) {
      StringBuilder sb = new StringBuilder();
      int initialColumn = column + 1;
      while (isLetter(peek()) || isDigit(peek()) || isSymbol(peek(), '_')) {
        sb.append((char) read());
        column++;
      }
      switch(sb.toString()) {
        case "and":
          return new Token(TokenType.AND, sb.toString(), line, initialColumn);
        case "or":
          return new Token(TokenType.OR, sb.toString(), line, initialColumn);
        case "not":
          return new Token(TokenType.NOT, sb.toString(), line, initialColumn);
        case "neg":
          return new Token(TokenType.NEG, sb.toString(), line, initialColumn);
        case "int":
          return new Token(TokenType.INT_TYPE, sb.toString(), line, initialColumn);
        case "double":
          return new Token(TokenType.DOUBLE_TYPE, sb.toString(), line, initialColumn);
        case "char":
          return new Token(TokenType.CHAR_TYPE, sb.toString(), line, initialColumn);
        case "string":
          return new Token(TokenType.STRING_TYPE, sb.toString(), line, initialColumn);
        case "bool":
          return new Token(TokenType.BOOL_TYPE, sb.toString(), line, initialColumn);
        case "void":
          return new Token(TokenType.VOID_TYPE, sb.toString(), line, initialColumn);
        case "var":
          return new Token(TokenType.VAR, sb.toString(), line, initialColumn);
        case "type":
          return new Token(TokenType.TYPE, sb.toString(), line, initialColumn);
        case "while":
          return new Token(TokenType.WHILE, sb.toString(), line, initialColumn);
        case "for":
          return new Token(TokenType.FOR, sb.toString(), line, initialColumn);
        case "from":
          return new Token(TokenType.FROM, sb.toString(), line, initialColumn);
        case "upto":
          return new Token(TokenType.UPTO, sb.toString(), line, initialColumn);
        case "downto":
          return new Token(TokenType.DOWNTO, sb.toString(), line, initialColumn);
        case "if":
          return new Token(TokenType.IF, sb.toString(), line, initialColumn);
        case "elif":
          return new Token(TokenType.ELIF, sb.toString(), line, initialColumn);
        case "else":
          return new Token(TokenType.ELSE, sb.toString(), line, initialColumn);
        case "fun":
          return new Token(TokenType.FUN, sb.toString(), line, initialColumn);
        case "new":
          return new Token(TokenType.NEW, sb.toString(), line, initialColumn);
        case "delete":
          return new Token(TokenType.DELETE, sb.toString(), line, initialColumn);
        case "return":
          return new Token(TokenType.RETURN, sb.toString(), line, initialColumn);
        case "nil":
          return new Token(TokenType.NIL, sb.toString(), line, initialColumn);
        case "true":
          return new Token(TokenType.BOOL_VAL, sb.toString(), line, initialColumn);
        case "false":
          return new Token(TokenType.BOOL_VAL, sb.toString(), line, initialColumn);
        default:
          return new Token(TokenType.ID, sb.toString(), line, initialColumn);
        
      }
    }

    error("invalid symbol '" + (char) read() + "'", line, ++column);
    return null;
  }
}
