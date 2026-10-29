package com.devsu.bank.cliente;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.devsu.bank.cliente",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule domainShouldNotDependOnOuterLayers =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application..", "..infrastructure..")
          .because("Domain is the innermost layer");

  @ArchTest
  static final ArchRule domainShouldBeFrameworkFree =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..", "jakarta..", "tools.jackson..")
          .because("Domain must be pure Java");

  @ArchTest
  static final ArchRule applicationShouldNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..infrastructure..", "org.springframework..", "jakarta..")
          .because("Use cases and ports depend only on the domain");

  @ArchTest
  static final ArchRule portsShouldBeInterfaces =
      classes()
          .that()
          .resideInAPackage("..application.port..")
          .should()
          .beInterfaces()
          .because("Ports are contracts");

  @ArchTest
  static final ArchRule useCasesShouldImplementInboundPort =
      classes()
          .that()
          .haveSimpleNameEndingWith("UseCase")
          .should()
          .implement(JavaClass.Predicates.resideInAPackage("..port.inbound.."))
          .because("Entry points need a contract; direction rules cannot see a missing interface");

  @ArchTest
  static final ArchRule adaptersShouldNotDependOnUseCases =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure..")
          .should()
          .dependOnClassesThat()
          .haveSimpleNameEndingWith("UseCase")
          .because("Adapters see the inbound port; only bootstrap wiring names the implementation");

  @ArchTest
  static final ArchRule entryPointsShouldNotDependOnDrivenAdapters =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure.entry.points..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure.driven.adapters..")
          .because("Entry points talk to ports, never to driven adapter implementations");

  @ArchTest
  static final ArchRule jpaShouldStayInPersistenceAdapter =
      noClasses()
          .that()
          .resideOutsideOfPackage("..infrastructure.driven.adapters.jpa..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
          .because("JPA is confined to the persistence adapter (ADR-0002)");

  @ArchTest
  static final ArchRule kafkaShouldStayInBrokerAdapter =
      noClasses()
          .that()
          .resideOutsideOfPackage("..infrastructure.driven.adapters.kafka..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework.kafka..", "org.apache.kafka..")
          .because(
              "Use cases publish through the outbox; broker SDKs belong to the dispatcher adapter");
}
