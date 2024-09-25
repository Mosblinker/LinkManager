/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.util.*;

/**
 * This is an iterator that wraps a given set's iterator. This is used to 
 * iterate through the elements in a set storing a cache of the elements to be 
 * iterated through, while also allowing the remove method to be able to 
 * actually remove an elements.
 * @author Milo Steier
 * @param <E> The type of elements returned by this iterator.
 */
public abstract class CacheSetIterator<E> implements Iterator<E>{
    /**
     * The iterator used to go through the cache of elements.
     */
    protected final Iterator<E> itr;
    /**
     * The element that was most recently returned by {@code next}.
     */
    private E current = null;
    /**
     * This constructs an iterator that will go through the set storing a 
     * cache of the elements.
     * @param cache The set containing a cache of elements.
     */
    public CacheSetIterator(Set<E> cache){
        this.itr = cache.iterator();
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public E next() {
        current = itr.next();
        return current;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void remove() {
        itr.remove();
        remove(current);
    }
    /**
     * This removes the given value from the actual collection that this 
     * iterator is suppose to be iterating through.
     * @param value The value to remove.
     * @throws UnsupportedOperationException If the remove operation is not 
     * supported.
     * @throws ConcurrentModificationException If the collection has been 
     * concurrently modified.
     */
    protected abstract void remove(E value);
}
