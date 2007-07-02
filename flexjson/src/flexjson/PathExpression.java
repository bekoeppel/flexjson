package flexjson;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: charlie
 * Date: Jun 27, 2007
 * Time: 11:36:34 PM
 */
public class PathExpression {
    String[] expression;
    boolean wildcard = false;

    public PathExpression( String expr ) {
        expression = expr.split("\\.");
        wildcard = expr.indexOf('*') >= 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for( int i = 0; i < expression.length; i++ ) {
            builder.append( expression[i] );
            if( i < expression.length - 1 ) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public boolean matches( Path path ) {
        int exprCurrentIndex = 0;
        int pathCurrentIndex = 0;
        while( pathCurrentIndex < path.length() ) {
            String current = path.getPath().get( pathCurrentIndex );
            if( exprCurrentIndex < expression.length && expression[exprCurrentIndex].equals("*") ) {
                exprCurrentIndex++;
            } else if( exprCurrentIndex < expression.length && expression[exprCurrentIndex].equals( current ) ) {
                pathCurrentIndex++;
                exprCurrentIndex++;
            } else if( exprCurrentIndex - 1 >= 0 && expression[exprCurrentIndex-1].equals("*") ) {
                pathCurrentIndex++;
            } else {
                return false;
            }
        }
        if( exprCurrentIndex > 0 && expression[exprCurrentIndex-1].equals("*") ) {
            return pathCurrentIndex >= path.length() && exprCurrentIndex >= expression.length;
        } else {
            return pathCurrentIndex >= path.length() && path.length() > 0;
        }
    }

    public boolean isWildcard() {
        return wildcard;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathExpression that = (PathExpression) o;

        if (!Arrays.equals(expression, that.expression)) return false;

        return true;
    }

    public int hashCode() {
        return (expression != null ? Arrays.hashCode(expression) : 0);
    }
}
