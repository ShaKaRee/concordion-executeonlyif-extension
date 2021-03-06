package org.concordion.ext.executeOnlyIf;

import org.concordion.api.AbstractCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.CommandCallList;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertListener;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.InvalidExpressionException;
import org.concordion.internal.util.Announcer;

public class ExecuteOnlyIfCommand extends AbstractCommand {

	private final Announcer<AssertListener> listeners = Announcer.to(AssertListener.class);

	public void addAssertListener(final AssertListener listener) {
		listeners.addListener(listener);
	}

	public void removeAssertListener(final AssertListener listener) {
		listeners.removeListener(listener);
	}

	@Override
	public void verify(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
		Element element = commandCall.getElement();
		String expression = commandCall.getExpression();
		Object result = evaluator.evaluate(expression);
		
		if (result != null && result instanceof Boolean) {
			CommandCallList childCommands = commandCall.getChildren();

			if ((Boolean) result) {
				// Execute all child commands and leaving displaying and reporting results up to them
				childCommands.setUp(evaluator, resultRecorder);
				childCommands.execute(evaluator, resultRecorder);
				childCommands.verify(evaluator, resultRecorder);
			} else {
				// Mark each child command as ignored and report as an ignored test
				for (int i = 0; i < childCommands.size(); i++) {
					resultRecorder.record(Result.IGNORED);
				}
				element.addStyleClass("ignored");
			}
		} else {
			throw new InvalidExpressionException("Expression '" + expression + "' did not produce a boolean result.");
		}
	}
}
