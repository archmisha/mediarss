package rss.rms.query.expression;

import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 20/05/13
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionBuilderHelper {
    private boolean lastProcessedInnerExpression = false;

    private static class SubTreeElement {
        private RmsQueryExpression firstExp;
        private LogicalExpressionSupport currRoot;
    }

    private Stack<SubTreeElement> elements;

    public ExpressionBuilderHelper() {
        elements = new Stack<>();
        elements.push(new SubTreeElement());
    }

    public void addLogicalExpression(LogicalExpressionSupport logicalOperator) {
        SubTreeElement currentSubTree = elements.peek();
        if (currentSubTree.currRoot == null) {
            currentSubTree.currRoot = logicalOperator;
            currentSubTree.currRoot.addChild(currentSubTree.firstExp);
        } else {
            logicalOperator.addChild(currentSubTree.currRoot);
            currentSubTree.currRoot = logicalOperator;
        }
    }

    public void addTerminalExpression(TerminalExpression terminalExpression) {
        SubTreeElement currentSubTree = elements.peek();
        if (currentSubTree.currRoot == null) {
            // this is the first Expression to be added - just remember it
            currentSubTree.firstExp = terminalExpression;
        } else {
            // there is already an expression in the root
            if (currentSubTree.currRoot instanceof AndLogicalExpressionSupport &&
                    currentSubTree.currRoot.getSubExpressions().get(0) instanceof OrLogicalExpressionSupport &&
                    !lastProcessedInnerExpression) {
                // this situation corresponds to the state when the 'or' operator comes before the current 'and' operator
                // in this case we expect to "rotate the tree": the and operator won't be stored in the currRoot variable,
                // instead, or operator will be preserved at the root of the tree, its last operand will become the first operand of the and operator
                // and the current operand will become the second operand of the 'and' operator. And operator will become the right operand of the or.
                // Example:
                // Assume we parse a || b && c
                // Assume, we have currently:
                //          &&
                //        /
                //      ||
                //    /   \
                //   a    b
                // Assume we've got 'c'
                // If it was a usual use case, the tree would look like this:
                //          &&
                //        /   \
                //      ||     c
                //    /   \
                //   a    b
                // However this means that there is no precedence of 'and' over 'or' operator. The tree corresponds to the following query:
                // (a || b) && c
                // So we have to 'rotate' the tree to be looking like this:
                //         ||
                //        /  \
                //       a   &&
                //          /  \
                //         b    c
                // All the above is applicable only when the last action we did was a subtree handling,
                // otherwise the query like (a || b ) && c will also have to pass this rotation so
                // the wrong rms will be produced

                OrLogicalExpressionSupport orExp = (OrLogicalExpressionSupport) currentSubTree.currRoot.getSubExpressions().get(0);
                int rightMostOrOperatorSubElemIndex = orExp.getSubExpressions().size() - 1;
                RmsQueryExpression rightMostOrOperatorSubElement = orExp.getSubExpressions().get(rightMostOrOperatorSubElemIndex);
                currentSubTree.currRoot.getSubExpressions().remove(0);
                currentSubTree.currRoot.addChild(rightMostOrOperatorSubElement);
                currentSubTree.currRoot.addChild(terminalExpression);
                orExp.getSubExpressions().remove(rightMostOrOperatorSubElemIndex);
                orExp.addChild(currentSubTree.currRoot);
                currentSubTree.currRoot = orExp;
            } else {

                currentSubTree.currRoot.addChild(terminalExpression);
            }
        }
        lastProcessedInnerExpression = false;

    }

    public void startSubTree() {
        elements.push(new SubTreeElement());
    }

    public void doneSubTree() {
        lastProcessedInnerExpression = true;
        SubTreeElement elem = elements.pop();
        if (elem.currRoot == null && elem.firstExp == null) {
            // empty sub tree - this can be if () was typed
            return;
        }
        RmsQueryExpression toAdd = null;
        if (elem.currRoot != null) {
            toAdd = elem.currRoot;
        } else {
            toAdd = elem.firstExp;
        }

        // this must be a complete logical expression
        SubTreeElement currElement = elements.peek();
        if (currElement.currRoot == null && currElement.firstExp == null) {
            // the previous expression was also empty (()):
            elements.pop();
            elements.push(elem);
        } else {
            if (currElement.currRoot != null) {
                currElement.currRoot.addChild(toAdd);
            }
        }

    }

    public RmsQueryExpression getExpression() {
        SubTreeElement root = elements.peek();
        if (root.currRoot != null) {
            return root.currRoot;
        } else {
            return root.firstExp;
        }
    }
}