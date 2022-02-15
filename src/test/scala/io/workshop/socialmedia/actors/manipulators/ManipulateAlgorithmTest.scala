package io.workshop.socialmedia.actors.manipulators

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ManipulateAlgorithmTest extends AnyWordSpec with Matchers {

  "Manipulate algorithm" should {
    "convert given text to the upper case with exclamation mark" in {
      // setup
      val myAwesomeText =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
          "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
          "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea " +
          "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum " +
          "dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, " +
          "sunt in culpa qui officia deserunt mollit anim id est laborum"

      val manipulatingResult = "LOREM IPSUM DOLOR SIT AMET, CONSECTETUR ADIPISCING ELIT, " +
        "SED DO EIUSMOD TEMPOR INCIDIDUNT UT LABORE ET DOLORE MAGNA ALIQUA. " +
        "UT ENIM AD MINIM VENIAM, QUIS NOSTRUD EXERCITATION ULLAMCO LABORIS NISI UT ALIQUIP EX EA " +
        "COMMODO CONSEQUAT. DUIS AUTE IRURE DOLOR IN REPREHENDERIT IN VOLUPTATE VELIT ESSE CILLUM " +
        "DOLORE EU FUGIAT NULLA PARIATUR. EXCEPTEUR SINT OCCAECAT CUPIDATAT NON PROIDENT, " +
        "SUNT IN CULPA QUI OFFICIA DESERUNT MOLLIT ANIM ID EST LABORUM!"

      // execute
      val result = ManipulateAlgorithm.makeUpperCaseWithExclamationMark(myAwesomeText)

      // assert
      assert(manipulatingResult == result)
    }
  }
}
