package org.tint.utils;

/**
 * User: Abhijit
 * Date: 2016-06-07
 */
public interface Predicate<T> {
    boolean isSatisfiedBy(T t);
}
