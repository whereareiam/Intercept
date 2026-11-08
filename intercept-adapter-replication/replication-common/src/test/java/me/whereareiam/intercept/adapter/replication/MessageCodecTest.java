package me.whereareiam.intercept.adapter.replication;

import me.whereareiam.intercept.model.replication.LanguageChosen;
import me.whereareiam.intercept.model.replication.ReloadRequested;
import me.whereareiam.intercept.model.replication.Replicated;
import me.whereareiam.intercept.model.replication.RevisionPublished;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageCodecTest {
	private static final UUID ALICE = UUID.fromString("10920508-d5d8-3eed-93d2-92f193afe7d7");

	@Test
	void everyMessageSurvivesTheLine() {
		roundTrip(new RevisionPublished(42));
		roundTrip(new LanguageChosen(ALICE, Locale.forLanguageTag("pt-BR")));
		roundTrip(new LanguageChosen(ALICE, null));
		roundTrip(new ReloadRequested());
	}

	@Test
	void aLineOfAnUnknownTypeIsIgnored() {
		assertNull(MessageCodec.decode("lobby-1 weather sunny"));
		assertNull(MessageCodec.decode("lobby-1"));
	}

	private static void roundTrip(Replicated message) {
		MessageCodec.Decoded decoded = MessageCodec.decode(MessageCodec.encode("lobby-1", message));

		assertEquals("lobby-1", decoded.instance());
		assertEquals(message, decoded.message());
	}
}
