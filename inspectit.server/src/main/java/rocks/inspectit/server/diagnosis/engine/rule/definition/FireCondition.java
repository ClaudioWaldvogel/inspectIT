package rocks.inspectit.server.diagnosis.engine.rule.definition;


import com.google.common.collect.ImmutableSet;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class FireCondition {

    private Set<String> tagTypes;

    public FireCondition(Set<String> tagTypes) {
        this.tagTypes = ImmutableSet.copyOf(tagTypes);
    }

    public boolean canFire(Set<String> offer) {
        checkNotNull(offer, "The offer must not be null!");
        return offer.containsAll(tagTypes);
    }

    //-------------------------------------------------------------
    // Methods: Accessors
    //-------------------------------------------------------------

    /**
     * Gets {@link #tagTypes}.
     *
     * @return {@link #tagTypes}
     */
    public Set<String> getTagTypes() {
        return tagTypes;
    }


    //-------------------------------------------------------------
    // Methods: Generated
    //-------------------------------------------------------------

    @Override
    public String toString() {
        return "FireCondition{" +
                "tagTypes=" + tagTypes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FireCondition that = (FireCondition) o;

        return getTagTypes() != null ? getTagTypes().equals(that.getTagTypes()) : that.getTagTypes() == null;

    }

    @Override
    public int hashCode() {
        return getTagTypes() != null ? getTagTypes().hashCode() : 0;
    }

}
