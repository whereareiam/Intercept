package me.whereareiam.intercept.interception.common.rule;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.interception.Surface;
import me.whereareiam.intercept.model.message.RuleFile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleFileReaderTest {
	private static final String ESSENTIALS = """
			no-permission:
			  pattern: "You don't have permission: ([\\\\w.]+)"
			  key: essentials.permission.missing
			  arguments:
			    permission: 1
			  surfaces: [chat, action-bar]
			  priority: 10
			  replace: whole
			broken:
			  pattern: "("
			  key: essentials.broken
			""";

	private final RuleFileReader reader = new RuleFileReader(
			Configura.builder().format(Format.YAML).build(),
			Logger.getAnonymousLogger()
	);

	@Test
	void aRuleIsCompiledAndABrokenOneSkipped() {
		List<Rule> rules = reader.read(List.of(new RuleFile("essentials.yml", ESSENTIALS, false)));

		assertEquals(1, rules.size());
		Rule rule = rules.getFirst();
		assertEquals("essentials/no-permission", rule.id());
		assertEquals(Set.of(Surface.CHAT, Surface.ACTION_BAR), rule.surfaces());
		assertTrue(rule.whole());

		Matcher matcher = rule.pattern().matcher("You don't have permission: essentials.fly");
		assertTrue(matcher.find());
		assertEquals(Map.of("permission", "essentials.fly"), rule.arguments(matcher));
	}

	@Test
	void aLocalRuleReplacesTheSharedOneOfTheSameFileAndName() {
		String local = """
				no-permission:
				  pattern: "Nope"
				  key: essentials.local
				""";

		List<Rule> rules = reader.read(List.of(
				new RuleFile("essentials.yml", ESSENTIALS, false),
				new RuleFile("essentials.yml", local, true)
		));

		assertEquals(1, rules.size());
		assertEquals("essentials.local", rules.getFirst().key());
		assertFalse(rules.getFirst().whole());
	}
}
