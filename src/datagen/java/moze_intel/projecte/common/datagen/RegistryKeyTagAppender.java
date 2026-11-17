package moze_intel.projecte.common.datagen;

import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/**
 * Thin wrapper around a {@link TagAppender} so call sites can name a single type parameter.
 * <p>
 * In 1.21.11 {@link TagAppender} takes two type parameters: the type of the elements that get added, and the type of the
 * entries of the tag. For the intrinsic holders providers these are the same, and the underlying appender already accepts
 * registry elements directly (it converts them to their {@link net.minecraft.resources.ResourceKey} internally), so this
 * wrapper only exists to keep the datagen providers readable.
 */
public final class RegistryKeyTagAppender<T> implements TagAppender<T, T> {

	private final TagAppender<T, T> delegate;

	private RegistryKeyTagAppender(TagAppender<T, T> delegate) {
		this.delegate = delegate;
	}

	public static <T> RegistryKeyTagAppender<T> wrap(TagAppender<T, T> delegate, Registry<T> registry) {
		//The registry is not needed, the delegate resolves the registry keys itself
		return new RegistryKeyTagAppender<>(delegate);
	}

	@Override
	public TagAppender<T, T> add(T value) {
		return delegate.add(value);
	}

	@Override
	public TagAppender<T, T> addOptional(T value) {
		return delegate.addOptional(value);
	}

	@Override
	public TagAppender<T, T> addTag(TagKey<T> tag) {
		return delegate.addTag(tag);
	}

	@Override
	public TagAppender<T, T> addOptionalTag(TagKey<T> tag) {
		return delegate.addOptionalTag(tag);
	}

	@Override
	public TagAppender<T, T> add(TagEntry entry) {
		return delegate.add(entry);
	}

	@Override
	public TagAppender<T, T> replace(boolean value) {
		return delegate.replace(value);
	}

	@Override
	public TagAppender<T, T> remove(T value) {
		return delegate.remove(value);
	}

	@Override
	public TagAppender<T, T> remove(TagKey<T> tag) {
		return delegate.remove(tag);
	}

	@Override
	public TagAppender<T, T> addAll(Collection<T> values) {
		return delegate.addAll(values);
	}

	@Override
	public TagAppender<T, T> addAll(Stream<T> values) {
		return delegate.addAll(values);
	}
}
