/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.operator;

import com.google.common.collect.ImmutableList;
import io.trino.spi.Page;
import io.trino.sql.planner.plan.PlanNodeId;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public class UpdateOperator
        extends AbstractRowChangeOperator
{
    public static class UpdateOperatorFactory
            implements OperatorFactory
    {
        private final int operatorId;
        private final PlanNodeId planNodeId;
        private final List<Integer> columnValueAndRowIdChannels;
        private boolean closed;

        public UpdateOperatorFactory(int operatorId, PlanNodeId planNodeId, List<Integer> columnValueAndRowIdChannels)
        {
            this.operatorId = operatorId;
            this.planNodeId = requireNonNull(planNodeId, "planNodeId is null");
            this.columnValueAndRowIdChannels = ImmutableList.copyOf(requireNonNull(columnValueAndRowIdChannels, "columnValueAndRowIdChannels is null"));
        }

        @Override
        public Operator createOperator(DriverContext driverContext)
        {
            checkState(!closed, "Factory is already closed");
            OperatorContext context = driverContext.addOperatorContext(operatorId, planNodeId, UpdateOperator.class.getSimpleName());
            return new UpdateOperator(context, columnValueAndRowIdChannels);
        }

        @Override
        public void noMoreOperators()
        {
            closed = true;
        }

        @Override
        public OperatorFactory duplicate()
        {
            return new UpdateOperatorFactory(operatorId, planNodeId, columnValueAndRowIdChannels);
        }
    }

    private final List<Integer> columnValueAndRowIdChannels;

    public UpdateOperator(OperatorContext operatorContext, List<Integer> columnValueAndRowIdChannels)
    {
        super(operatorContext);
        this.columnValueAndRowIdChannels = columnValueAndRowIdChannels;
    }

    @Override
    public void addInput(Page page)
    {
        requireNonNull(page, "page is null");
        checkState(state == State.RUNNING, "Operator is %s", state);

        // Call the UpdatablePageSource to update rows in the page supplied.
        pageSource().updateRows(page, columnValueAndRowIdChannels);
        rowCount += page.getPositionCount();
    }
}
