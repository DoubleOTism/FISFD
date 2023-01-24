# Fisfd
# Anotace:  
Aplikace pro přidávání filmů do uživatelských katalogů a jejich prohlížení.   
#  Členové týmu 
* Adam Šindler(sina07)
* Andrei Nechiporuk(neca02)
* Eduard Wolf(wole00)
* Erich Pross(proe11)
* Jakub Vojta(vojj08)
* Tomáš Augustin(augt00)

# Zadání úlohy
Aplikace slouží pro výběr filmů z databáze, registrovaný a přihlášený uživatel si může vybraný film přidat do svého filmového listu. V tomto listu si přihlášený uživatel může film nastavit do jedné z tří kategorii a to "Plánuji sledovat", "Sleduji", "Hotovo".   
Nepřihlášený uživatel může tuto aplikaci využívat pouze jako databázi filmů, muže si stejně jako přihlášený uživatel filtrovat filmy dle žánrů.   
Uživatel si při registraci bude moci vybrat jaké žánry preferuje a zároveň jaký je jeho oblíbený film a dle tohoto výběru mu bude doporučen film který by se mu mohl líbit.   
  
# User stories  
* Jako neregistrovaný uživatel si chci zobrazit filmovou databázi, filtrovat si jí podle žánrů.
* Jako registrovaný a přihlášený uživatel si chci vytvořit list filmů na které se chci dívat, které aktuálně sleduju, a které sem již viděl.   
* Jako registrovaný uživatel chci aby mi aplikace doporučija film dle mého oblíbého žánru.

# Deployment 
Pro automatické tvoření jar souborů z naší aplikace používáme Maven v kombinaci s gitlab CI/CD.
V Mavenu jsou nastavené všechny potřebné dependencies pro správné fungování aplikace a následně pluginem maven-assembly-plugin nastavena tvorba fat jaru, tzn. jar file který obsahuje všechny potřebné dependencies v sobě zakomponované. Následně pomocí gitlab-ci.yml souboru je nastaveno, že po každém commitu se má kompilovat nový jar soubor pomocí mvn package (gitlab si pomocí docker containeru všechno buildne). 
Aktuální limitace jsou v načítání XML databází, problém s jar pathama. 