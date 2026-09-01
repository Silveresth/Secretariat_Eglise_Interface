package com.eglise.secretariat;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class OcrRegexTest {

    private final String sampleText = """
            Fait à Lomé, le 23/08/2026
            Le Pasteur
            EGLISE DES ASSEMBLEES DE DIEU DU TOGO
            TEMPLE DIEU NE CHANGE PAS
            04 BP: 27 Lomé 04-Togo Tél: 0028 90 14 68 87
            LETTRE DE RECOMMANDATION
            Objet : Transfert X Voyage Emploi 
            Je soussigné : Pasteur AD Pasteur de l'Église des Assemblées de Dieu Temple : « DIEU NE
            CHANGE PAS », atteste que le ou la nommé(e) : KOFFI Jean-Baptiste est membre actif
            de notre assemblée depuis : 15/01/2018 .
            Date et lieu de naissance : 12/05/1995 à Lomé
            Baptisé d'eau le : 10/04/2016
            Baptisé du Saint-Esprit le : 04/06/2017
            Profession : Informaticien
            Situation matrimoniale : Célibataire Fiancé(e) Marié(e) X Divorcé(e) Nombre d'enfant : 
            3
            Sexe : Masculin X Féminin 
            Sous-discipline : oui non X Date de levée de la discipline :
            ACTIVITÉ AU SEIN DE L'ÉGLISE LOCALE
            Diacre/Diaconesse Conseil des hommes Ministère des femmes 
            Jeunesse Interprète Groupe musical Choriste 
            Évangéliste Moniteur Accueil 
            AUTRE RENSEIGNEMENT
            Motif / Église de destination : Transfert pour raison professionnelle - Église : Temple de Kpalimé
            En foi de quoi, nous lui délivrons la présente lettre de recommandation pour servir et valoir ce que de droit.
            « Recevez-le en notre seigneur d'une manière digne des saints et que vous l'assistiez dans les choses dont il 
            aurait besoin...... » Romain 16 :1-2.
            """;

    @Test
    public void testExtractionNomPrenomsVariations() {
        // Variation 1: Standard sample
        assertEquals("KOFFI Jean-Baptiste", extractNomPrenomSmart(sampleText));

        // Variation 2: OCR with noise on "est membre"
        String noisy1 = "CHANGE PAS », atteste que le ou la nommé(e) : KOFFI Jean-Baptiste est mernbre actif de notre";
        assertEquals("KOFFI Jean-Baptiste", extractNomPrenomSmart(noisy1));

        // Variation 3: OCR with dots
        String noisy2 = "atteste que le ou la nommé(e) : ........ KOFFI Jean-Baptiste ........ est membre";
        assertEquals("KOFFI Jean-Baptiste", extractNomPrenomSmart(noisy2));

        // Variation 4: OCR with name on NEXT line
        String noisy3 = "atteste que le ou la nommé(e) :\nKOFFI Jean-Baptiste\nest membre actif";
        assertEquals("KOFFI Jean-Baptiste", extractNomPrenomSmart(noisy3));

        // Variation 5: "nom et prénoms :"
        String noisy4 = "Nom et prénoms : KOUASSI Paul-Marie\nDate de naissance : 10/02/1990";
        assertEquals("KOUASSI Paul-Marie", extractNomPrenomSmart(noisy4));
    }

    public static String extractNomPrenomSmart(String text) {
        if (text == null || text.isBlank()) return null;

        String[] lines = text.split("[\\r\\n]+");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            String lower = line.toLowerCase();

            // Case A : Line contains trigger keywords
            if (lower.contains("nomm") || lower.contains("atteste que") || lower.contains("porteur") || lower.contains("nom et") || lower.contains("nom :")) {
                int colonIdx = line.lastIndexOf(':');
                if (colonIdx != -1) {
                    String afterColon = line.substring(colonIdx + 1).trim();
                    afterColon = afterColon.replaceAll("(?i)\\s+est\\b.*$", "");
                    afterColon = afterColon.replaceAll("(?i)\\s+(?:de\\s+notre|depuis|date\\s*de).*$", "");
                    afterColon = afterColon.replaceAll("^[.:_\\s«»\"'…\\-]+|[.:_\\s«»\"'…\\-]+$", "").trim();

                    if (isValidCandidateName(afterColon)) {
                        return afterColon;
                    }

                    // If after colon is empty, check the NEXT line
                    if (afterColon.isBlank() && i + 1 < lines.length) {
                        String nextLine = lines[i + 1].trim();
                        nextLine = nextLine.replaceAll("(?i)\\s+est\\b.*$", "");
                        nextLine = nextLine.replaceAll("(?i)\\s+(?:de\\s+notre|depuis|date\\s*de).*$", "");
                        nextLine = nextLine.replaceAll("^[.:_\\s«»\"'…\\-]+|[.:_\\s«»\"'…\\-]+$", "").trim();
                        if (isValidCandidateName(nextLine)) {
                            return nextLine;
                        }
                    }
                } else {
                    String stripped = line.replaceAll("(?i)^.*?(?:nomm[a-z()\\/\\s]*|atteste\\s+que(?:\\s+le\\s+ou\\s+la)?|porteur\\s*du\\s*pr[ée]sent|porteur)\\s*", "");
                    stripped = stripped.replaceAll("(?i)\\s+est\\b.*$", "");
                    stripped = stripped.replaceAll("(?i)\\s+(?:de\\s+notre|depuis|date\\s*de).*$", "");
                    stripped = stripped.replaceAll("^[.:_\\s«»\"'…\\-]+|[.:_\\s«»\"'…\\-]+$", "").trim();
                    if (isValidCandidateName(stripped)) {
                        return stripped;
                    }
                }
            }
        }

        // Case B : Regex search for UPPERCASE word followed by Capitalized words
        Pattern pUpper = Pattern.compile("([A-ZÀ-ÿ\\-]{2,}\\s+[A-ZÀ-ÿ][a-zà-ÿ\\-]+(?:\\s+[A-ZÀ-ÿ][a-zà-ÿ\\-]+)*)");
        Matcher mUpper = pUpper.matcher(text);
        while (mUpper.find()) {
            String cand = mUpper.group(1).trim();
            if (isValidCandidateName(cand) && !cand.contains("ASSEMBLEES") && !cand.contains("DIEU") && !cand.contains("LETTRE") && !cand.contains("RECOMMANDATION")) {
                return cand;
            }
        }

        return null;
    }

    private static boolean isValidCandidateName(String str) {
        if (str == null || str.isBlank()) return false;
        if (str.length() < 3 || str.length() > 60) return false;
        String lower = str.toLowerCase();
        if (lower.equals("le pasteur") || lower.contains("eglise") || lower.contains("temple") || lower.contains("recommandation") || lower.contains("assemblees") || lower.contains("dieu ne change pas")) {
            return false;
        }
        return str.matches(".*[A-Za-zÀ-ÿ]{2,}.*");
    }

    @Test
    public void testExtractionDateNaissance() {
        Pattern birthPat = Pattern.compile("(?i)(?:date\\s*et\\s*lieu\\s*de\\s*naissance|n[ée]\\s*\\(e\\)\\s*le|naissance)\\s*:?\\s*(\\d{1,2}[\\/\\-\\.]\\d{1,2}[\\/\\-\\.]\\d{4})(?:\\s*[àa]\\s*([^\\r\\n]+))?");
        Matcher birthMat = birthPat.matcher(sampleText);
        assertTrue(birthMat.find());
        assertEquals("12/05/1995", birthMat.group(1));
        assertNotNull(birthMat.group(2));
        assertEquals("Lomé", birthMat.group(2).trim());
    }

    @Test
    public void testDateOcrYearCorrection() {
        LocalDate d1 = parseDateWithOcrFix("10/04/2076");
        assertNotNull(d1);
        assertEquals(2016, d1.getYear());
        assertEquals(4, d1.getMonthValue());
        assertEquals(10, d1.getDayOfMonth());

        LocalDate d2 = parseDateWithOcrFix("04/06/2077");
        assertNotNull(d2);
        assertEquals(2017, d2.getYear());
        assertEquals(6, d2.getMonthValue());
        assertEquals(4, d2.getDayOfMonth());
    }

    private LocalDate parseDateWithOcrFix(String str) {
        if (str == null) return null;
        try {
            String[] parts = str.trim().split("[\\/\\-\\.]");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0].trim());
                int month = Integer.parseInt(parts[1].trim());
                int year = Integer.parseInt(parts[2].trim());
                if (year >= 2070 && year <= 2079) {
                    year = year - 60;
                }
                return LocalDate.of(year, month, day);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
