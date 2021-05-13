package droid64.d64;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CyclicGuard<T> {
	private final Set<T> set = new LinkedHashSet<>();

	public void add(T item) throws CbmException {
		if (set.contains(item)) {
			throw new CbmException("Already seen " +  item + ".");
		}
		set.add(item);
	}

	public boolean addSilent(T item) {
		if (set.contains(item)) {
			return false;
		}
		set.add(item);
		return true;
	}


	public boolean contains(T item) {
		return set.contains(item);
	}

	public int size() {
		return set.size();
	}

	public Stream<T> stream() {
		return set.stream();
	}

	@Override
	public String toString() {
		return set.stream().map(T::toString).collect(Collectors.joining(", "));
	}
}
