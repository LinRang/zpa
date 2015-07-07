package br.com.felipezorzo.sonar.plsql.api;

import static br.com.felipezorzo.sonar.plsql.api.PlSqlKeyword.*;
import static br.com.felipezorzo.sonar.plsql.api.PlSqlPunctuator.*;
import static br.com.felipezorzo.sonar.plsql.api.PlSqlTokenType.*;
import static com.sonar.sslr.api.GenericTokenType.EOF;
import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;

import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerfulGrammarBuilder;

public enum PlSqlGrammar implements GrammarRuleKey {
    
    // Data types
    DATATYPE,
    NUMERIC_DATATYPE,
    LOB_DATATYPE,
    CHARACTER_DATAYPE,
    BOOLEAN_DATATYPE,
    DATE_DATATYPE,
    
    // Literals
    LITERAL,
    BOOLEAN_LITERAL,
    NULL_LITERAL,
    NUMERIC_LITERAL,
    CHARACTER_LITERAL,
    
    // Expressions
    EXPRESSION,
    CHARACTER_EXPRESSION,
    BOOLEAN_EXPRESSION,
    DATE_EXPRESSION,
    NUMERIC_EXPRESSION,
    
    // Statements
    BLOCK_STATEMENT,
    NULL_STATEMENT,
    ASSIGNMENT_STATEMENT,
    IF_STATEMENT,
    LOOP_STATEMENT,
    EXIT_STATEMENT,
    CONTINUE_STATEMENT,
    FOR_STATEMENT,
    WHILE_STATEMENT,
    RETURN_STATEMENT,
    STATEMENT,
    
    // Declarations
    VARIABLE_DECLARATION,
    PARAMETER_DECLARATION,
    HOST_AND_INDICATOR_VARIABLE,
    
    DECLARE_SECTION,
    EXCEPTION_HANDLER,
    IDENTIFIER_NAME,
    EXECUTE_PLSQL_BUFFER,
    
    // Program units
    ANONYMOUS_BLOCK,
    PROCEDURE_DECLARATION,
    FUNCTION_DECLARATION,
    CREATE_PROCEDURE,
    CREATE_FUNCTION,
    
    // Top-level components
    FILE_INPUT;

    public static LexerfulGrammarBuilder create() {
        LexerfulGrammarBuilder b = LexerfulGrammarBuilder.create();

        b.rule(IDENTIFIER_NAME).is(IDENTIFIER);
        b.rule(FILE_INPUT).is(b.oneOrMore(b.firstOf(ANONYMOUS_BLOCK, CREATE_PROCEDURE, CREATE_FUNCTION)), EOF);

        createLiterals(b);
        createDatatypes(b);
        createStatements(b);
        createExpressions(b);
        createDeclarations(b);
        createProgramUnits(b);
        
        b.setRootRule(FILE_INPUT);
        b.buildWithMemoizationOfMatchesForAllRules();
        
        return b;
    }
    
    private static void createLiterals(LexerfulGrammarBuilder b) {
        b.rule(NULL_LITERAL).is(NULL);
        b.rule(BOOLEAN_LITERAL).is(b.firstOf(TRUE, FALSE));
        b.rule(NUMERIC_LITERAL).is(b.firstOf(INTEGER_LITERAL, REAL_LITERAL, SCIENTIFIC_LITERAL));
        b.rule(CHARACTER_LITERAL).is(STRING_LITERAL);
        
        b.rule(LITERAL).is(b.firstOf(NULL_LITERAL, BOOLEAN_LITERAL, NUMERIC_LITERAL, CHARACTER_LITERAL));
    }
    
    private static void createDatatypes(LexerfulGrammarBuilder b) {
        b.rule(NUMERIC_DATATYPE).is(
                b.firstOf(
                        BINARY_DOUBLE,
                        BINARY_FLOAT,
                        BINARY_INTEGER,
                        DEC,
                        DECIMAL,
                        b.sequence(DOUBLE, PRECISION),
                        FLOAT,
                        INT,
                        INTEGER,
                        NATURAL,
                        NATURALN,
                        NUMBER,
                        NUMERIC,
                        PLS_INTEGER,
                        POSITIVE,
                        POSITIVEN,
                        REAL,
                        SIGNTYPE,
                        SMALLINT), 
                b.optional(LPARENTHESIS, INTEGER_LITERAL, b.optional(COMMA, INTEGER_LITERAL), RPARENTHESIS));
        
        b.rule(LOB_DATATYPE).is(b.firstOf(BFILE, BLOB, CLOB, NCLOB));
        
        b.rule(CHARACTER_DATAYPE).is(
                b.firstOf(
                        CHAR,
                        CHARACTER,
                        LONG,
                        b.sequence(LONG, RAW),
                        NCHAR,
                        NVARCHAR2,
                        RAW,
                        ROWID,
                        STRING,
                        UROWID,
                        VARCHAR,
                        VARCHAR2), 
                b.optional(LPARENTHESIS, INTEGER_LITERAL, RPARENTHESIS));
        
        b.rule(BOOLEAN_DATATYPE).is(BOOLEAN);
        
        b.rule(DATE_DATATYPE).is(DATE);
        
        b.rule(DATATYPE).is(b.firstOf(NUMERIC_DATATYPE, LOB_DATATYPE, CHARACTER_DATAYPE, BOOLEAN_DATATYPE, DATE_DATATYPE));
    }

    private static void createStatements(LexerfulGrammarBuilder b) {
        b.rule(HOST_AND_INDICATOR_VARIABLE).is(COLON, IDENTIFIER_NAME, b.optional(COLON, IDENTIFIER_NAME));
        
        b.rule(VARIABLE_DECLARATION).is(IDENTIFIER_NAME,
                                        b.optional(CONSTANT),
                                        DATATYPE,
                                        b.optional(b.optional(NOT, NULL), b.firstOf(ASSIGNMENT, DEFAULT), LITERAL),
                                        SEMICOLON);
        
        b.rule(NULL_STATEMENT).is(NULL, SEMICOLON);
        
        b.rule(EXCEPTION_HANDLER).is(WHEN, b.firstOf(OTHERS, IDENTIFIER_NAME), THEN, b.oneOrMore(STATEMENT));
        
        b.rule(BLOCK_STATEMENT).is(BEGIN, b.oneOrMore(STATEMENT), b.optional(EXCEPTION, b.oneOrMore(EXCEPTION_HANDLER)), END, SEMICOLON);
        
        b.rule(ASSIGNMENT_STATEMENT).is(
                b.firstOf(b.sequence(IDENTIFIER_NAME,
                                     b.optional(b.firstOf(b.sequence(DOT, IDENTIFIER_NAME),
                                                          b.sequence(LPARENTHESIS, NUMERIC_EXPRESSION, RPARENTHESIS)))),
                          HOST_AND_INDICATOR_VARIABLE),
                ASSIGNMENT,
                EXPRESSION,
                SEMICOLON);
        
        b.rule(IF_STATEMENT).is(
                IF, BOOLEAN_EXPRESSION, THEN,
                b.oneOrMore(STATEMENT),
                b.zeroOrMore(ELSIF, BOOLEAN_EXPRESSION, THEN, b.oneOrMore(STATEMENT)),
                b.optional(ELSE, b.oneOrMore(STATEMENT)),
                END, IF, SEMICOLON);
        
        b.rule(LOOP_STATEMENT).is(LOOP, b.oneOrMore(STATEMENT), END, LOOP, SEMICOLON);
        
        b.rule(EXIT_STATEMENT).is(EXIT, b.optional(WHEN, BOOLEAN_EXPRESSION), SEMICOLON);
        
        b.rule(CONTINUE_STATEMENT).is(CONTINUE, b.optional(WHEN, BOOLEAN_EXPRESSION), SEMICOLON);
        
        b.rule(FOR_STATEMENT).is(
                FOR, IDENTIFIER_NAME, IN, b.optional(REVERSE), NUMERIC_EXPRESSION, RANGE, NUMERIC_EXPRESSION, LOOP,
                b.oneOrMore(STATEMENT),
                END, LOOP, SEMICOLON);
        
        b.rule(WHILE_STATEMENT).is(
                WHILE, BOOLEAN_EXPRESSION, LOOP,
                b.oneOrMore(STATEMENT),
                END, LOOP, SEMICOLON);
        
        b.rule(RETURN_STATEMENT).is(RETURN, b.optional(EXPRESSION), SEMICOLON);
        
        b.rule(STATEMENT).is(b.firstOf(NULL_STATEMENT,
                                       BLOCK_STATEMENT,
                                       ASSIGNMENT_STATEMENT, 
                                       IF_STATEMENT, 
                                       LOOP_STATEMENT, 
                                       EXIT_STATEMENT, 
                                       CONTINUE_STATEMENT,
                                       FOR_STATEMENT,
                                       WHILE_STATEMENT,
                                       RETURN_STATEMENT));
    }
    
    private static void createExpressions(LexerfulGrammarBuilder b) {
        // Reference: http://docs.oracle.com/cd/B28359_01/appdev.111/b28370/expression.htm
        b.rule(CHARACTER_EXPRESSION).is(
               b.firstOf(STRING_LITERAL, IDENTIFIER_NAME, HOST_AND_INDICATOR_VARIABLE),
               b.optional(CONCATENATION, CHARACTER_EXPRESSION));
        
        b.rule(BOOLEAN_EXPRESSION).is(
               b.optional(NOT),
               b.firstOf(BOOLEAN_LITERAL, IDENTIFIER_NAME),
               b.optional(b.firstOf(AND, OR), BOOLEAN_EXPRESSION));
        
        b.rule(DATE_EXPRESSION).is(
               b.firstOf(DATE_LITERAL, IDENTIFIER_NAME, HOST_AND_INDICATOR_VARIABLE),
               b.optional(b.firstOf(PLUS, MINUS), NUMERIC_EXPRESSION));
        
        b.rule(NUMERIC_EXPRESSION).is(
               b.firstOf(NUMERIC_LITERAL,
                         b.sequence(b.firstOf(IDENTIFIER_NAME, HOST_AND_INDICATOR_VARIABLE), 
                             b.optional(
                                 b.firstOf(b.sequence(MOD, ROWCOUNT),
                                           b.sequence(DOT, b.firstOf(COUNT,
                                                                     FIRST,
                                                                     LAST,
                                                                     LIMIT,
                                                                     b.sequence(b.firstOf(NEXT, PRIOR),
                                                                                LPARENTHESIS,
                                                                                NUMERIC_EXPRESSION,
                                                                                RPARENTHESIS)))
                                              ))),
                         b.sequence(SQL, MOD, b.firstOf(ROWCOUNT,
                                                        b.sequence(BULK_ROWCOUNT, LPARENTHESIS, NUMERIC_EXPRESSION, RPARENTHESIS))),
                         HOST_AND_INDICATOR_VARIABLE),
               b.optional(EXPONENTIATION, NUMERIC_EXPRESSION),
               b.optional(b.firstOf(PLUS, MINUS, MULTIPLICATION, DIVISION), NUMERIC_EXPRESSION));
        
        b.rule(EXPRESSION).is(b.firstOf(CHARACTER_EXPRESSION, BOOLEAN_EXPRESSION, DATE_EXPRESSION, NUMERIC_EXPRESSION));
    }
    
    private static void createDeclarations(LexerfulGrammarBuilder b) {
        b.rule(PARAMETER_DECLARATION).is(
                IDENTIFIER_NAME,
                b.optional(IN),
                b.firstOf(
                        b.sequence(DATATYPE, b.optional(b.firstOf(ASSIGNMENT, DEFAULT), EXPRESSION)),
                        b.sequence(OUT, b.optional(NOCOPY), DATATYPE))
                );
    }
    
    private static void createProgramUnits(LexerfulGrammarBuilder b) {
        b.rule(EXECUTE_PLSQL_BUFFER).is(DIVISION);
        
        b.rule(DECLARE_SECTION).is(b.oneOrMore(VARIABLE_DECLARATION));
        
        // http://docs.oracle.com/cd/B28359_01/appdev.111/b28370/procedure.htm
        b.rule(PROCEDURE_DECLARATION).is(
                PROCEDURE, IDENTIFIER_NAME,
                b.optional(LPARENTHESIS, b.oneOrMore(PARAMETER_DECLARATION, b.optional(COMMA)), RPARENTHESIS),
                b.firstOf(
                        SEMICOLON,
                        b.sequence(b.firstOf(IS, AS), b.zeroOrMore(DECLARE_SECTION), BLOCK_STATEMENT))
                );
        
        // http://docs.oracle.com/cd/B28359_01/appdev.111/b28370/function.htm
        b.rule(FUNCTION_DECLARATION).is(
                FUNCTION, IDENTIFIER_NAME,
                b.optional(LPARENTHESIS, b.oneOrMore(PARAMETER_DECLARATION, b.optional(COMMA)), RPARENTHESIS),
                RETURN, DATATYPE,
                b.firstOf(
                        SEMICOLON,
                        b.sequence(b.firstOf(IS, AS), b.zeroOrMore(DECLARE_SECTION), BLOCK_STATEMENT))
                );
        
        // http://docs.oracle.com/cd/B28359_01/appdev.111/b28370/create_procedure.htm
        b.rule(CREATE_PROCEDURE).is(
                CREATE, b.optional(OR, REPLACE),
                PROCEDURE, b.optional(IDENTIFIER_NAME, DOT), IDENTIFIER_NAME,
                b.optional(LPARENTHESIS, b.oneOrMore(PARAMETER_DECLARATION, b.optional(COMMA)), RPARENTHESIS),
                b.optional(AUTHID, b.firstOf(CURRENT_USER, DEFINER)),
                b.firstOf(IS, AS),
                b.firstOf(
                        b.sequence(b.zeroOrMore(DECLARE_SECTION), BLOCK_STATEMENT),
                        b.sequence(LANGUAGE, JAVA, STRING_LITERAL, SEMICOLON),
                        b.sequence(EXTERNAL, SEMICOLON))
                );
        
        // http://docs.oracle.com/cd/B28359_01/appdev.111/b28370/create_function.htm
        b.rule(CREATE_FUNCTION).is(
                CREATE, b.optional(OR, REPLACE),
                FUNCTION, b.optional(IDENTIFIER_NAME, DOT), IDENTIFIER_NAME,
                b.optional(LPARENTHESIS, b.oneOrMore(PARAMETER_DECLARATION, b.optional(COMMA)), RPARENTHESIS),
                RETURN, DATATYPE,
                b.optional(AUTHID, b.firstOf(CURRENT_USER, DEFINER)),
                b.firstOf(IS, AS),
                b.firstOf(
                        b.sequence(b.zeroOrMore(DECLARE_SECTION), BLOCK_STATEMENT),
                        b.sequence(LANGUAGE, JAVA, STRING_LITERAL, SEMICOLON))
                );
        
        b.rule(ANONYMOUS_BLOCK).is(
                b.optional(DECLARE, DECLARE_SECTION),
                BLOCK_STATEMENT,
                EXECUTE_PLSQL_BUFFER
                );
    }
}
