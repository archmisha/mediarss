package rss.commons.query.api;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rss.commons.query.api.model.*;
import rss.commons.query.generated.QueryLexer;
import rss.commons.query.generated.QueryParser;

import java.util.BitSet;

/**
 * This class implements the {@link QueryParser} interface. This implementation
 * wraps the ANTLR 4 generated parser and provides a convenient interface for common query expressions parsing tasks.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/4/13
 */
class QueryParserImpl implements rss.commons.query.api.QueryParser {

    private static final Logger LOGGER = LogManager.getLogger(QueryParserImpl.class);
    private boolean antlrTrace = false;

    /**
     * {@inheritDoc}
     */
    public QueryLayout parseLayout(String layoutExpression) {
        return parseTree(new QueryLayoutParseHandler(createAntlrParser(layoutExpression)));
    }

    public QueryGroup parseGroup(String groupExpression) {
        return parseTree(new QueryGroupParseHandler(createAntlrParser(groupExpression)));
    }

    /**
     * {@inheritDoc}
     */
    public QueryFilter parseFilter(String filterExpression) {
        return parseTree(new QueryFilterParseHandler(createAntlrParser(filterExpression)));
    }

    /**
     * {@inheritDoc}
     */
    public QueryOrder parseOrder(String orderExpression) {
        return parseTree(new QueryOrderParseHandler(createAntlrParser(orderExpression)));
    }

    /**
     * {@inheritDoc}
     */
    public QueryMeta parseMeta(String metaExpression) {
        return parseTree(new QueryMetaParseHandler(createAntlrParser(metaExpression)));
    }

    /**
     * {@inheritDoc}
     */
    public QueryPage parsePage(String pageExpression) {
        return parseTree(new QueryPageParseHandler(createAntlrParser(pageExpression)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int parseSkip(String skipNumberExpression) {
        return parseUnsignedParameterValue(skipNumberExpression, "illegal.page.param", "skip");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int parseSize(String sizeNumberExpression) {
        return parseUnsignedParameterValue(sizeNumberExpression, "illegal.page.param", "size");
    }

    /**
     * Sets the ANTLR trace flag - for developer debug purposes only.
     *
     * @param antlrTrace the trace value, either {@code true} or {@code false}.
     */
    public final void setAntlrTrace(boolean antlrTrace) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Setting ANTLR trace to {}", antlrTrace);
        }
        this.antlrTrace = antlrTrace;
    }

    private <T> T parseTree(AbstractQueryParseHandler<T> parseHandler) {
        try {
            return parseHandler.parseTree();
        } catch (QueryParserException e) {
            throw e;
        } catch (Exception e) {
            /*
             * We never seems to get here, but it's a good safety net to prevent unwanted exception types from being
             * thrown when something goes wrong.
             */
            throw new QueryParserException(e);
        }
    }

    /**
     * This method creates and returns an ANTLR generated parser for the input expression.
     *
     * @param inputExpression parser input expression
     * @return a parser instance.
     */
    private QueryParser createAntlrParser(String inputExpression) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating ANTLR parser for input expression: {}", inputExpression);
        }

        CharStream input = new ANTLRInputStream(inputExpression.toCharArray(), inputExpression.length());
        QueryLexer lexer = new QueryLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QueryParser parser = new QueryParser(tokens);

        if (antlrTrace) {
            parser.setBuildParseTree(true);
            parser.setTrace(true);
        }

        // Replacing the default error handlers to ensure strict syntax enforcement.
        parser.setErrorHandler(new BailErrorStrategy());
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line, int charPositionInLine, String msg, @Nullable RecognitionException e) {
                LOGGER.error(QueryParserImpl.class.getSimpleName() + " line " + line + ":" + charPositionInLine + " " + msg);
            }

            @Override
            public void reportAmbiguity(@NotNull Parser recognizer, @NotNull DFA dfa, int startIndex, int stopIndex, boolean exact, @NotNull BitSet ambigAlts, @NotNull ATNConfigSet configs) {
            }

            @Override
            public void reportAttemptingFullContext(@NotNull Parser recognizer, @NotNull DFA dfa, int startIndex, int stopIndex, @Nullable BitSet conflictingAlts, @NotNull ATNConfigSet configs) {
            }

            @Override
            public void reportContextSensitivity(@NotNull Parser recognizer, @NotNull DFA dfa, int startIndex, int stopIndex, int prediction, @NotNull ATNConfigSet configs) {
            }
        });
        return parser;

    }

    private int parseUnsignedParameterValue(String numberExpression, String messageKey, String parameterName) {
        int intValue;

        try {
            intValue = Integer.parseInt(numberExpression);
        } catch (NumberFormatException e) {
            throw new QueryParserException("Illegal value for parameter: '" + parameterName + "'.", e);
        }

        if (intValue < 0) {
            throw new QueryParserException("'" + parameterName + "' must be positive.");
        }

        return intValue;
    }

}
