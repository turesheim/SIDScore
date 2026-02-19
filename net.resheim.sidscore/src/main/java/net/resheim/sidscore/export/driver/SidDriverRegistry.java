/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.export.driver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Registry for built-in and plugin-provided SID driver backends.
 */
public final class SidDriverRegistry {
	private final Map<String, SidDriverBackend> backends;

	private SidDriverRegistry(Map<String, SidDriverBackend> backends) {
		this.backends = backends;
	}

	public static SidDriverRegistry load() {
		Map<String, SidDriverBackend> resolved = new LinkedHashMap<>();

		SidDriverBackend sidScore = new SidScoreDriverBackend();
		resolved.put(normalizeId(sidScore.id()), sidScore);

		for (SidDriverBackend backend : ServiceLoader.load(SidDriverBackend.class)) {
			String id = normalizeId(backend.id());
			if (!resolved.containsKey(id)) {
				resolved.put(id, backend);
			}
		}
		return new SidDriverRegistry(Collections.unmodifiableMap(new LinkedHashMap<>(resolved)));
	}

	public Collection<SidDriverBackend> list() {
		return backends.values();
	}

	public Optional<SidDriverBackend> find(String id) {
		if (id == null || id.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(backends.get(normalizeId(id)));
	}

	private static String normalizeId(String id) {
		return id == null ? "" : id.trim().toLowerCase();
	}
}
