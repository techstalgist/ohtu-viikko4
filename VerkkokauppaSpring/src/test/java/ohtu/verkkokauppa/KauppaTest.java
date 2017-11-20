package ohtu.verkkokauppa;

import org.junit.*;
import static org.mockito.Mockito.*;

public class KauppaTest {
    Kauppa kauppa;
    Pankki pankki;
    Viitegeneraattori viite;
    Varasto varasto;
    
    @Before
    public void setUp() {
         // luodaan ensin mock-oliot
        pankki = mock(Pankki.class);

        viite = mock(Viitegeneraattori.class);
        // määritellään että viitegeneraattori palauttaa viitten 42 ja seuraavaksi 43
        when(viite.uusi()).thenReturn(42).thenReturn(43);
        
        varasto = mock(Varasto.class);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.saldo(2)).thenReturn(5);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "leipä", 4));
        when(varasto.saldo(3)).thenReturn(0);
        when(varasto.haeTuote(3)).thenReturn(new Tuote(3, "kahvi", 3));
        kauppa = new Kauppa(varasto, pankki, viite);  
    }
    
    @Test
    public void ostoksenPaatyttyaPankinMetodiaTilisiirtoKutsutaan() {

        // tehdään ostokset
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        kauppa.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        //String nimi, int viitenumero, String tililta, String tilille, int summa
        verify(pankki).tilisiirto("pekka", 42, "12345", "33333-44455", 5);   
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    
    @Test
    public void tilisiirtoaKutsutaanOikeinKunOstettuKahtaEriTuotetta() {

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto("pekka", 42, "12345", "33333-44455", 9);
    }
    
    @Test
    public void tilisiirtoaKutsutaanOikeinKunOstettuSamaaTuotettaKaksiKpl() {

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto("pekka", 42, "12345", "33333-44455", 10);
    }
    
    @Test
    public void tilisiirtoaKutsutaanOikeinKunYritettyOstaaTuoteJonkaSaldoNollaJaTuoteJotaOnSaatavilla() {

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(3);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto("pekka", 42, "12345", "33333-44455", 5);
    }
    
    @Test
    public void asioinninAloittaminenTyhjentaaOstoskorin() {
        
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), eq("33333-44455"), eq(5));
        
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("saara", "23456");
        
        verify(pankki).tilisiirto(eq("saara"), anyInt(), eq("23456"), eq("33333-44455"), eq(4));
    }
    
    @Test
    public void kauppaPyytaaUudenViitteenJokaiselleMaksulle() {
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), anyInt());
        
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("saara", "23456");
        
        verify(pankki).tilisiirto(eq("saara"), eq(43), eq("23456"), eq("33333-44455"), anyInt());
    }
    
    @Test
    public void tuotteenPoistaminenKoristaAiheuttaaPalautuksenVarastoon() {
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.poistaKorista(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto("pekka", 42, "12345", "33333-44455", 4);
    }
}
