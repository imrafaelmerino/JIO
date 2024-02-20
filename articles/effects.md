**Introducción**

En este post vamos a introducir el concepto de Monads y explicar las ideas fundamentales de la
librería JIO.

En la charla _**How To Design A Good API and Why it Matters**_, Joshua Blosh comenta que hay que
evitar la transliteración de APIs. Este es precisamente el problema que existe en el ecosistema Java
con algunas librerías de programación funcional (en lo sucesivo FP). Son traducciones literales de
otros lenguajes, lo que termina en API complejos y nada familiares para el programador. Precisamente
el reto de JIO es beneficiarse de ideas de la FP pero adecuando la sintaxis a un Java muy estándar.

En lo sucesivo hablaremos indistintamente de métodos o funciones salvo que indiquemos de forma
explícita lo contrario. No obstante, cabe destacar que existe una diferencia fundamental en Java (no
así en otros lenguajes). Los métodos no se pueden utilizar como argumentos de entrada de otros
métodos o funciones, mientras que las funciones sí. Es más, aquellas funciones que tienen como
parámetros de entrada otras funciones se les conoce en la jerga funcional como **High Order
Functions**.

**Un poco de historia**

Haskell se diseñó como un lenguaje funcional puro. A modo de recordatorio, esto significa que
utiliza funciones puras en el sentido matemático, es decir, son funciones que dados unos parámetros
de entrada (inputs) producen un resultado (output) y no realizan nada adicional (ausencia de
side-effects). Por otro lado los inputs y outputs son immutables, lo que se conoce como values.

Ejemplos típicos que no representan funciones puras en el mundo de los lenguajes imperativos son los
métodos que carecen de parámetros de entrada o no retornan nada (indicado con la palabra reservada
void).

Si una función no retorna nada, es obvio que lleva asociado algún tipo de side effect, como
modificar el estado de una estructura de datos, realizar algún tipo de acción como imprimir por
pantalla un texto etc. De lo contrario, ¿para que que se ha sido invocada?

Otra característica de las funciones puras es que los mismos inputs deben de producir **siempre** el
mismo resultado. Parece una propiedad lógica, ¡después de todo uno más uno es siempre dos! Pero hay
un gran número de escenarios donde esta propiedad no es válida. Por ejemplo, la misma consulta a una
base de datos puede retornar distintos resultados dependiendo del momento en que se realice, desde
un número diferente de registros a una excepción como un timeout o cierre de conexión.

En definitiva, a pesar de las bondades de las funciones puras no es posible utilizarlas para
implementar un gran número de problemas que se dan en la vida real. ¿Y qúe hizo Haskell para lidiar
con este tipo de problemas?

¡Pues en sus inicios ho hizo nada!. No era trivial para ellos poder resolver este rompecabezas sin
renunciar a otros de sus pilares fundamentales: **laziness**. Side-effects y laziness no encajan
bien.

Cuando se quiere realizar algún tipo de acción (imprimir o leer un carácter por pantalla, activar un
dispositivo, lo que sea...) se necesita tener control absoluto del instante en que se ejecute.
Precisamente cuando una expresión es lazy pierdes ese control, ya que al estar trabajando con
funciones puras y values el momento en el que se evalúan las funciones es irrelevante para el
resultado final del programa, que será siempre el mismo.

En definitiva, **Haskell en sus inicios era un lenguaje sin poder implementar ningún tipo de IO
(Input/Output) para interaccionar con el mundo exterior**.

Como indica Simon Peyton Jones en
[Escape from the ivory tower: the Haskell journey](https://www.youtube.com/watch?v=re96UgMk6GQ),
pasaron algo de vergüenza durante alguno años y tuvieron que aguantar más que una broma de sus
colegas en las conferencias a las que asistían.

Una famosa viñeta de [xkcd](https://xkcd.com) que ahora entenderás es la siguiente:

<img src="https://imgs.xkcd.com/comics/haskell.png">

Todo esto cambió de forma radical cuando Philip Wadler, más conocido como Super Lambda Man (enlace
al video), introdujo el concepto de Monads en 1992, dos años después de la primera versión de
Haskell. No se me ocurre mejor forma de finalizar este apartado que parafraseando las palabras con
las que Philip comienza la charla
[Category Theory for the Working Hacker](https://www.google.com/search?client=safari&rls=en&q=Category+Theory+for+the+Working+Hacker&ie=UTF-8&oe=UTF-8):

_Gracias a todos por venir, especialmente tan tarde, aunque supongo que es pronto en España. El
hecho de que 260 personas de la industria quieran aprender programación funcional, es algo que si
pudiera volver 30 años atrás y contar al comité de Haskell, dirían ¡estás de broma! Pero hay una
lección aquí que quiero que te lleves, y es que si quieres cambiar el mundo y es lo que deberías
hacer, puedes hacerlo, de la misma manera que lo hicimos nosotros, que es, encuentra una buena idea
y encuentra gente increíble que trabaje en ella junto a ti. Nunca tendríamos type classes en Haskell
sin el trabajo que Mark Jones hizo a partir del mío. Nunca hubieran inventando Monads sin el trabajo
de [Kleisli](https://en.wikipedia.org/wiki/Heinrich_Kleisli) y
[Eugenio Moggi](https://en.wikipedia.org/wiki/Eugenio_Moggi). Por lo tanto es siempre una cadena. Te
invito a unirte a esta cadena. Recuerda, encuentra buenas ideas y grandes personas para trabajar
codo con codo en ellas. Y aquí viene lo importante (y mi parte favorita), todo el mundo te ignorará.
Y esto es lo que he aprendido ahora:no te preocupes, ¡tan solo sigue haciendo lo mismo durante
treinta años!_

**Monads en Java**

Desde la versión 8 hemos estado trabajando con Monads en Java. Por lo tanto explicaremos la idea
fundamental directamente en este lenguaje.

Imagina la siguiente función

```java

Function<Person, Address> getAddress

```

En un mundo puro está claro que esa función siempre retorna una dirección (nada de null), nunca
produce un error lanzando una excepción, y no hay ningún tipo de interacción con el exterior (no
consulta una BBDD o llama a un microservicio).

Pero Java no te obliga a trabajar con funciones puras, y cualquiera de los side-effects mencionados
más arriba pueden ocurrir. El programador no tiene otra opción que ver como está la función
implementada para saber todos los posibles desenlaces.

Empecemos por retornar null. ¿Cómo hacemos para indicar esta posibilidad? Utilizando un Monad. En
Java se llama Optional. En Haskell Maybe. En Scala Option.

```java

Function<Person, Optional<Address>> getAddress

```

Hemos convertido una función impura en pura o un side-effect en un funcional -effect. Es una función
más honesta que la anterior al reflejar claramente la posibilidad de que una persona no tenga
ninguna dirección asociada.

Pongamos más ejemplos de side-effects que estás habituado a trabajar en Java, quizás sin saberlo.
Supongamos que el anterior método getAddress tiene que realizar una consulta a una base datos. **No
es lo mismo realizar un cálculo en local, del orden de los nanosegundos, a tener que establecer una
conexión TCP con un servidor localizado a miles de Km**. Es un hecho digno de indicar en la
signatura de una función para advertir al programador de los peligros asociados. Y si no estás de
acuerdo es posible que cambies de opinión con la lectura de
[Fallacies of distributed computing](https://en.wikipedia.org/wiki/Fallacies_of_distributed_computing)

**Cuando se trata de indicar que una operación lleva una latencia asociada el Monad a utilizar es
Future.**

```java

Function<Person, Future<Address>> getAddress

```

El crecimiento exponencial de internet provocó problemas de escalabilidad en los servidores,
problema que se conoce como
[C10K](https://en.wikipedia.org/wiki/C10k_problem#:~:text=The%20C10k%20problem%20is%20the,concurrently%20handling%20ten%20thousand%20connections.).
En Java la implementación de Future bloquea el thread en espera el resultado, de ahí que surgiera
una nueva implementación en la versión 8 que permite trabajar de forma asíncrona: CompletableFuture.
Haremos un uso extensivo de esta estructura en JIO.

Otro aspecto a resaltar de CompletableFuture es que es una abstracción que puede representar tanto
un resultado correcto como cualquier fallo producido durante la obtención de la dirección. **Es
vital que los errores sean simples values que siempre aparecen como valor de retorno de las
funciones y no como excepciones que se lazan de un lado a otro.** Esta es la razón que hace que la
FP facilite el hacer programas más resilientes. Como veremos, será uno de los puntos fuertes de JIO.

Imagina por un momento que los zipcodes se encuentran en una caché que hay que leer y que la
operación puede lanzar algún tipo de excepción, como que la caché está vacía por ejemplo.
Necesitamos indicar este hecho en la signatura de la función. Para ello existe el monad Try, que no
existe en Java, pero si en otros lenguajes como Scala.

```java

Function<Person, Try<Address>> getAddress

```

La diferencia con Future es que si bien ambos monads pueden representar un fallo, Try no lleva
ninguna latencia asociada. En el siguiente apartado, lo implementaremos a modo de ejemplo.

Y por supuesto podemos combinar los Monads anteriores. Funciones con latencia que pueden no retornar
ningún resultado:

```java

Function<Person, CompletableFuture<Optional<Address>>> getAddress

```

O funciones sin latencia, que pueden fallar con un excepción o no retornar ningún resultado:

```java

Function<Person, Try<Optional<Address>>> getAddress

```

Un Monad es una abstración que permite convertir un side-effect en lo que se conoce como un
funcional-effect o simplemente effect. Los side-effects son directamente bugs en un mundo funcional
ya que hacen nuestras funcionas **dishonestas** al realizar acciones que no son reflejadas en sus
signaturas.

Por otro lado podemos generalizar indicando que un Monad presenta la forma de un tipo parametrizado
M<R> donde R es el tipo del valor retornado (en nuestro caso Address) y M el monad per se, cuyo tipo
dependerá del side effect que representa (en nuestro caso Optional, Future o Try).

**Ejemplo práctico: Implementación del monad Try en Java**

Todo monad tiene dos operaciones fundamentales que en la jerga funcional se han llamado
tradicionalmente pure o return y bind. pure es simplemente un constructor para crear monads, para
pasar del mundo impuro de side-effects al mundo puro. En Java, en vez de pure, los constructores
suelen aparecer con otro nombre en forma de static factory methods.

Antes de desarrollar el monad Try, veamos algunos ejemplos con optional y future convirtiendo
nuestra función impura de ejemplo en pura, libre de side-effects.

```java

// función impura al poder retornar una dirección nula
Function<Person, Address> getAddressImpure

// función pura
Function<Person, Optional<Address>> getAddress =
        person -> person == null ?
                    Optional.empty() :
                    Optional.of(getAddressImpure.apply(person));


// más conciso con el constructor ofNullable
Function<Person, Optional<Address>> getAddress =
        person -> Optional.ofNullable(getAddressImpure.apply(person));
```

Veamos el caso de CompletableFuture

```java

// función impura ya que realiza llamada a bbdd en data center a 10000 km
Function<Person, Address> getAddressImpure

// completedFuture and failedFuture
Function<Person, CompletableFuture<Address>> getAddress =
      person -> {
              try
              {
                return CompletableFuture.completedFuture(getAddressImpure.apply(person));
              }
              catch (Exception e)
              {
                return CompletableFuture.failedFuture(e);
              }
            };

```

Ahora la función indica que lleva una latencia asociada y los errores son simples values que se
retornan, como si de una dirección se tratase.

Antes de continuar y explicar la segunda operación bind, vamos a implementar nuestro primer monad en
Java:

```java

public abstract sealed class Try<O> permits Try.Success, Try.Failure {

    public static <O> Try<O> succeed(O result) {
        return new Success<>(result);
    }

    public static <O> Try<O> fail(Exception e) {
        return new Failure(e).cast();
    }

    public O get() throws Exception {
        return switch (this) {
            case Try.Failure failure -> throw failure.exception;
            case Try.Success<O> success -> success.result;
        };
    }

    public <U> Try<U> flatMap(Function<O, Try<U>> fn) {
        return switch (this) {
            case Try.Failure failure -> failure.cast();
            case Try.Success<O> success -> fn.apply(success.result);
        };
    }

    public <U> Try<U> map(Function<O, U> fn) {
        return switch (this) {
            case Try.Failure failure -> failure.cast();
            case Try.Success<O> success -> Try.succeed(fn.apply(success.result));
        };
    }

    static final class Success<O> extends Try<O> {
        final O result;

        Success(O result) {
            this.result = result;
        }
    }

    static final class Failure extends Try<Object> {

        //Failure has no parametrized type -> down-casting is safe
        @SuppressWarnings("unchecked")
        private <U> Try<U> cast() {
            return (Try<U>) this;
        }

        final Exception exception;

        Failure(Exception failure) {
            this.exception = failure;
        }
    }
}

```

Como se observa Try es un sealed type con dos posibles subtipos: **Success** and **Failure**. Los
subtipos son privados y, por lo tanto, están ocultos al cliente del API, **que solo tendrá que
manejar Try**. Los constructores son los métodos estáticos **succeed** y **fail**. Del mismo modo
que hay constructores para pasar del mundo impuro al puro, ¡necesitamos una manera de hacer el viaje
de regreso! Para eso está el método **get**. Es un método que al final hay que invocar, pero hay una
gran diferencia entre hacerlo una vez, para obtener el resultado final, lo que es correcto, a
hacerlo constantemente, con los inconveniences ya explicados asociados al side effect.

En este caso hemos empleado pattern-matching para implementar los métodos get, map y flatMap (que
veremos a continuación). Es necesario realizar un casting (método **cast**). Es buena práctica
eliminar todos los warnings del compilador si no suponen ningún riesgo con la anotación
SuppressWarning, indicando en un comentario la razón de porque se suprime.

Ya podemos utilizar nuestro monad Try en nuestro ejemplo para el caso de que la función getAddress
pudiese fallar con una excepción por cualquier motivo:

```java

// posible excepción al obtener la dirección: función impura
Function<Person, Address> getAddressImpure

//
Function<Person, Try<Address>> getAddress =
      person -> {
              try {
                    return Try.success(getAddressImpure.apply(person));
                } catch (Exception e) {
                    return Try.fail(e);
                }
            };

```

Como se observa el patrón es siempre el mismo: utilizar un constructor u otro del monad en función
del desenlace de invocar a la función impura.

La segunda operación asociada a un monad es bind o más comumente conocidad como flatMap. La FP es un
paradigma que resuelve los problemas descomponiéndolos en funciones pequeñas y componiendo dichas
funciones. Componiendo funciones es como lidiamos con la complejidad, del mismo modo que en la
programación orientada a objectos es la encapsulación y polimorfismo. Cada paradigma tiene su receta
para descomponer problemas complejos en problemas más sencillos.

Pero el introducir un tipo nuevo, tiene un incoveniente. Si bien en el mundo impuro la siguiente
operación es posible sintácticamente hablando:

```java

Function<String,Person> getPersonById;

Function<Person,Address> getAddress;

Function<String,Address> getAddressById = getPersonById.andThen(getAddress);

```

En el mundo puro de los monads:

```java

Function<String, Optional<Person>> getPersonById;

Function<Person, Optional<Address>> getAddress;

// no compile!
Function<String,Optional<Address>> getAddressById = getPersonById.andThen(getAddress);

```

no es posible. La función getAddress tiene como parámetro de entrada un objeto de tipo Person y la
función getPersonById retorna un Optional, con lo cual el programa anterior no compila. ¡ Hemos
perdido la capacidad de componer funciones ! Y aquí es donde el operador bind o flapMap nos ayuda:

```java

Function<String,Optional<Address>> getAddressById = getPersonById.flatMap(getAddress);

```

La anterior operación es perfectamente válida y además está libre de NullPointerException sin que el
programador tenga que preocuparse de nada.

En definitiva, el operador flatMap nos permite componer funciones que retornan monads.

Vamos a implementar el método flatMap de nuestro monad Try

```java

public <U> Try<U> flatMap(Function<O, Try<U>> fn) {
    return switch (this) {
            case Try.Failure<U> failure -> failure;
            case Try.Success<O> success -> fn.apply(success.result);
    };
}


getPersonById.flatMap(getAddress)

```

En el ejemplo el tipo O corresponde a Person y el tipo U a Address. Aplicamos pattern matching sobre
el el resultado de evaluar la primera funcion, getPerson, y si el resultado es un error, no podemos
continuar, se retorna directamente, mientras que si el resultado es correcto y obtenemos un tipo
Person, aplicamos la función getAddress para obtener una posible dirección.

Como se observa, a la hora de combinar funciones, es el monad quien absorve la responsabilidad de
chequar si una operación falla o no, liberando al programador de dicha carga y facilitando la
operación de composición, que comos hemos reiterado en varias ocasiones es una pieza clave en el
mundo de la FP para lidiar con la complejidad.

Las piezas claves que hay que recordar es que un monad permite transformar una operación con un
side-effect en una función pura, y que el operador flatMap te permite combinar funciones puras que
retornan monads.

Volvamos a hablar de CompletableFuture, pieza clave en JIO. Para ello piensa cuántas veces se
imprimiría por pantalla Hello world! en el siguiente programa:

```java


CompletableFuture.all(List.of(CompletableFuture.supplyAsync(() -> System.out.println("!Hello world!")),
                              CompletableFuture.supplyAsync(() -> System.out.println("!Hello world!"))
                             )
                      )
                 .join();


```

```java

CompletableFuture cf =
        CompletableFuture.supplyAsync(() -> System.out.println("!Hello world!"));

CompletableFuture.all(List.of(cf, cf)).join();

```

```java

Supplier<CompletableFuture> cf =
        () -> CompletableFuture.supplyAsync(() -> System.out.println("Hello world!"));

CompletableFuture.all(List.of(cf.get(), cf.get())).join();

```

```java

public sealed abstract class IO<O> implements Supplier<CompletableFuture<O>> permits Exp, Val {}

final class Val<O> extends IO<O> {}

sealed abstract class Exp<O> extends IO<O>
        permits AllExp, AnyExp, CondExp, IfElseExp, JsArrayExp, JsObjExp, ListExp, SwitchExp, PairExp, TripleExp {}

```

expresiones vs values

api y operaciones típicas: recoverWith, recover, repeat, retry

Creación de servidores en Java Creación de clientes http Creación de clientes de base de datos
Testing: random testing y pbt Monitorización y logging: JFR y contextual logging
