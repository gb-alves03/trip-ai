package com.trip_ai.infra.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.trip_ai.domain.port.out.IataResolverPort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class IataResolverService implements IataResolverPort {

    // Normalized key: lowercase, accents stripped, commas/parens removed
    private static final Map<String, String> KNOWN = Map.ofEntries(
        // Brasil
        Map.entry("sao paulo", "GRU"), Map.entry("são paulo", "GRU"),
        Map.entry("guarulhos", "GRU"), Map.entry("congonhas", "CGH"),
        Map.entry("campinas", "VCP"), Map.entry("viracopos", "VCP"),
        Map.entry("rio de janeiro", "GIG"), Map.entry("galeao", "GIG"),
        Map.entry("galeão", "GIG"), Map.entry("santos dumont", "SDU"),
        Map.entry("brasilia", "BSB"), Map.entry("brasília", "BSB"),
        Map.entry("salvador", "SSA"), Map.entry("belo horizonte", "CNF"),
        Map.entry("confins", "CNF"), Map.entry("curitiba", "CWB"),
        Map.entry("porto alegre", "POA"), Map.entry("recife", "REC"),
        Map.entry("fortaleza", "FOR"), Map.entry("manaus", "MAO"),
        Map.entry("belem", "BEL"), Map.entry("belém", "BEL"),
        Map.entry("florianopolis", "FLN"), Map.entry("florianópolis", "FLN"),
        Map.entry("natal", "NAT"), Map.entry("maceio", "MCZ"),
        Map.entry("maceió", "MCZ"), Map.entry("joao pessoa", "JPA"),
        Map.entry("joão pessoa", "JPA"), Map.entry("aracaju", "AJU"),
        Map.entry("teresina", "THE"), Map.entry("vitoria", "VIX"),
        Map.entry("vitória", "VIX"), Map.entry("cuiaba", "CGB"),
        Map.entry("cuiabá", "CGB"), Map.entry("goiania", "GYN"),
        Map.entry("goiânia", "GYN"), Map.entry("campo grande", "CGR"),
        Map.entry("porto velho", "PVH"), Map.entry("macapa", "MCP"),
        Map.entry("macapá", "MCP"), Map.entry("boa vista", "BVB"),
        Map.entry("palmas", "PMW"), Map.entry("rio branco", "RBR"),
        Map.entry("joinville", "JOI"), Map.entry("londrina", "LDB"),
        Map.entry("maringa", "MGF"), Map.entry("maringá", "MGF"),
        Map.entry("uberlandia", "UDI"), Map.entry("uberlândia", "UDI"),
        Map.entry("ilheus", "IOS"), Map.entry("ilhéus", "IOS"),
        Map.entry("porto seguro", "BPS"), Map.entry("foz do iguacu", "IGU"),
        Map.entry("foz do iguaçu", "IGU"), Map.entry("barreiras", "BRA"),

        // América do Sul
        Map.entry("buenos aires", "EZE"), Map.entry("ezeiza", "EZE"),
        Map.entry("aeroparque", "AEP"), Map.entry("montevideo", "MVD"),
        Map.entry("montevidéu", "MVD"), Map.entry("santiago", "SCL"),
        Map.entry("santiago chile", "SCL"), Map.entry("lima", "LIM"),
        Map.entry("bogota", "BOG"), Map.entry("bogotá", "BOG"),
        Map.entry("caracas", "CCS"), Map.entry("quito", "UIO"),
        Map.entry("guayaquil", "GYE"), Map.entry("la paz", "LPB"),
        Map.entry("asuncion", "ASU"), Map.entry("assunção", "ASU"),
        Map.entry("paramaribo", "PBM"), Map.entry("georgetown", "GEO"),

        // América do Norte
        Map.entry("nova york", "JFK"), Map.entry("new york", "JFK"),
        Map.entry("nova iorque", "JFK"), Map.entry("miami", "MIA"),
        Map.entry("orlando", "MCO"), Map.entry("los angeles", "LAX"),
        Map.entry("chicago", "ORD"), Map.entry("san francisco", "SFO"),
        Map.entry("seattle", "SEA"), Map.entry("boston", "BOS"),
        Map.entry("washington", "IAD"), Map.entry("houston", "IAH"),
        Map.entry("dallas", "DFW"), Map.entry("atlanta", "ATL"),
        Map.entry("denver", "DEN"), Map.entry("las vegas", "LAS"),
        Map.entry("phoenix", "PHX"), Map.entry("toronto", "YYZ"),
        Map.entry("vancouver", "YVR"), Map.entry("montreal", "YUL"),
        Map.entry("ciudad de mexico", "MEX"), Map.entry("cancun", "CUN"),
        Map.entry("cancún", "CUN"), Map.entry("havana", "HAV"),
        Map.entry("havana cuba", "HAV"),

        // Europa
        Map.entry("lisboa", "LIS"), Map.entry("lisbon", "LIS"),
        Map.entry("porto", "OPO"), Map.entry("madrid", "MAD"),
        Map.entry("barcelona", "BCN"), Map.entry("paris", "CDG"),
        Map.entry("londres", "LHR"), Map.entry("london", "LHR"),
        Map.entry("amsterdam", "AMS"), Map.entry("amsterda", "AMS"),
        Map.entry("frankfurt", "FRA"), Map.entry("berlim", "BER"),
        Map.entry("berlin", "BER"), Map.entry("roma", "FCO"),
        Map.entry("rome", "FCO"), Map.entry("milao", "MXP"),
        Map.entry("milão", "MXP"), Map.entry("milan", "MXP"),
        Map.entry("zurique", "ZRH"), Map.entry("zurich", "ZRH"),
        Map.entry("genebra", "GVA"), Map.entry("geneva", "GVA"),
        Map.entry("bruxelas", "BRU"), Map.entry("brussels", "BRU"),
        Map.entry("viena", "VIE"), Map.entry("vienna", "VIE"),
        Map.entry("copenhague", "CPH"), Map.entry("copenhagen", "CPH"),
        Map.entry("estocolmo", "ARN"), Map.entry("stockholm", "ARN"),
        Map.entry("oslo", "OSL"), Map.entry("helsinki", "HEL"),
        Map.entry("varsovia", "WAW"), Map.entry("warsaw", "WAW"),
        Map.entry("praga", "PRG"), Map.entry("prague", "PRG"),
        Map.entry("budapeste", "BUD"), Map.entry("budapest", "BUD"),
        Map.entry("atenas", "ATH"), Map.entry("athens", "ATH"),
        Map.entry("moscou", "SVO"), Map.entry("moscow", "SVO"),
        Map.entry("istambul", "IST"), Map.entry("istanbul", "IST"),
        Map.entry("dublin", "DUB"), Map.entry("edimburgo", "EDI"),
        Map.entry("edinburgh", "EDI"),

        // Ásia / Oceania / África
        Map.entry("dubai", "DXB"), Map.entry("doha", "DOH"),
        Map.entry("abu dhabi", "AUH"), Map.entry("tokyo", "NRT"),
        Map.entry("toquio", "NRT"), Map.entry("tóquio", "NRT"),
        Map.entry("osaka", "KIX"), Map.entry("seoul", "ICN"),
        Map.entry("seul", "ICN"), Map.entry("beijing", "PEK"),
        Map.entry("pequim", "PEK"), Map.entry("shanghai", "PVG"),
        Map.entry("xangai", "PVG"), Map.entry("hong kong", "HKG"),
        Map.entry("singapura", "SIN"), Map.entry("singapore", "SIN"),
        Map.entry("bangkok", "BKK"), Map.entry("bali", "DPS"),
        Map.entry("denpasar", "DPS"), Map.entry("sydney", "SYD"),
        Map.entry("melbourne", "MEL"), Map.entry("auckland", "AKL"),
        Map.entry("johannesburg", "JNB"), Map.entry("johannesburgo", "JNB"),
        Map.entry("cape town", "CPT"), Map.entry("cidade do cabo", "CPT"),
        Map.entry("cairo", "CAI"), Map.entry("nairobi", "NBO"),
        Map.entry("mumbai", "BOM"), Map.entry("delhi", "DEL"),
        Map.entry("nova delhi", "DEL"), Map.entry("kuala lumpur", "KUL")
    );

    @Value("${tripai.claude.api-key}")
    private String apiKey;

    @Value("${tripai.claude.model}")
    private String model;

    private AnthropicClient client;

    @PostConstruct
    void init() {
        client = AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    }

    @Override
    public String resolve(String cityOrCode) {
        String trimmed = cityOrCode.trim().toUpperCase();
        if (trimmed.matches("[A-Z]{3}")) return trimmed;

        // Fast local lookup — normalize: lowercase, strip trailing country hints
        String normalized = cityOrCode.trim().toLowerCase()
            .replaceAll("[,()]", "")   // remove commas and parens
            .replaceAll("\\s+", " ")
            .strip();

        String fromMap = KNOWN.get(normalized);
        if (fromMap != null) {
            log.info("[IATA] '{}' → '{}' (mapa local)", cityOrCode, fromMap);
            return fromMap;
        }

        // Also try without anything after a comma (e.g. "Santiago, Chile" → "Santiago")
        int comma = normalized.indexOf(',');
        if (comma > 0) {
            String beforeComma = normalized.substring(0, comma).strip();
            fromMap = KNOWN.get(beforeComma);
            if (fromMap != null) {
                log.info("[IATA] '{}' → '{}' (mapa local, truncado)", cityOrCode, fromMap);
                return fromMap;
            }
        }

        try {
            Message response = client.messages().create(
                MessageCreateParams.builder()
                    .model(model)
                    .maxTokens(16L)
                    .system("""
                        You are an airport IATA code resolver.
                        Given a city name or airport name, respond with ONLY the 3-letter IATA code \
                        of its main commercial airport.
                        If the input is already a valid 3-letter IATA code, return it unchanged.
                        Respond with ONLY the 3-letter uppercase code — no explanation, no punctuation.""")
                    .addUserMessage(cityOrCode.trim())
                    .build()
            );

            String iata = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text().trim().toUpperCase())
                .filter(s -> s.matches("[A-Z]{3}"))
                .findFirst()
                .orElse(null);

            if (iata != null) {
                log.info("[IATA] '{}' → '{}' (Claude)", cityOrCode, iata);
                return iata;
            }

            log.warn("[IATA] Resposta inválida do Claude para '{}', usando fallback", cityOrCode);
        } catch (Exception e) {
            log.warn("[IATA] Falha ao resolver '{}' via Claude (sem créditos?): {}", cityOrCode, e.getMessage());
        }

        return trimmed.length() >= 3 ? trimmed.substring(0, 3) : trimmed;
    }
}
