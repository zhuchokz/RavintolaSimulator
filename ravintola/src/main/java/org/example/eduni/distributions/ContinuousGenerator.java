package org.example.eduni.distributions;

import org.example.eduni.distributions.Seedable;

/** A continuous generator provides a double value according to the distribution it relies on.
 */
public interface ContinuousGenerator extends Seedable {
    double sample(); 
}
