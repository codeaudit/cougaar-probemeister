set JAVA_HOME=c:\j2sdk1.4.2_03
set path=%JAVA_HOME%\bin;%path%
set PM=K:\CVS\probemeister-20050215\probemeister
set JRP=K:\CVS\probemeister-20040924\jreversepro\jreversepro-1.4.2a-src
set classpath=%PM%\examples;%PM%\lib\probemeister.jar;%PM%\lib\bcel5.jar;%JRP%\lib\jreversepro.jar;%PM%\lib\lark.jar;%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\jre\lib\rt.jar

java -version

java com.objs.surveyor.probemeister.gui.ProbeMeisterGUI -listen 9876 
