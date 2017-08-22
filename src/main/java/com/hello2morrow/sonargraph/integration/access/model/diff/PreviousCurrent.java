/**
 * Sonargraph Integration Access
 * Copyright (C) 2016-2017 hello2morrow GmbH
 * mailto: support AT hello2morrow DOT com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hello2morrow.sonargraph.integration.access.model.diff;

import java.io.Serializable;

public final class PreviousCurrent<T1> implements Serializable
{
    private static final long serialVersionUID = 1643574306425192701L;
    private final T1 previous;
    private final T1 current;

    public PreviousCurrent(final T1 previous, final T1 current)
    {
        assert previous != null : "Parameter 'previous' of method 'PreviousCurrent' must not be null";
        assert current != null : "Parameter 'current' of method 'PreviousCurrent' must not be null";
        assert previous != current : "Same instances";
        this.previous = previous;
        this.current = current;
    }

    public T1 getPrevious()
    {
        return previous;
    }

    public T1 getCurrent()
    {
        return current;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + previous.hashCode();
        result = prime * result + current.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        final PreviousCurrent<?> other = (PreviousCurrent<?>) obj;
        return previous.equals(other.previous) && current.equals(other.current);
    }

    @Override
    public String toString()
    {
        return previous + "|" + current;
    }
}