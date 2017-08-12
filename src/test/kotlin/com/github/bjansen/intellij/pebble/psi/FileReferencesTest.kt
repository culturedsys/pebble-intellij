package com.github.bjansen.intellij.pebble.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import org.junit.Test

class FileReferencesTest : LightCodeInsightFixtureTestCase() {

    override fun getTestDataPath(): String = "src/test/resources/references"

    private fun initFile(vararg files: String): PebbleFile {
        val configuredFiles = myFixture.configureByFiles(*files)
        return configuredFiles[0] as PebbleFile
    }

    private fun moveCaret(columnShift: Int, lineShift: Int) {
        myFixture.editor.caretModel.moveCaretRelatively(columnShift, lineShift, false, false, true)
    }

    private fun findReferencedFile(file: PebbleFile): PsiFile? {
        val elementAtCaret = file.findElementAt(myFixture.caretOffset)
        val element = PsiTreeUtil.getParentOfType(elementAtCaret, PebbleTagDirective::class.java)

        if (element != null) {
            element.references.forEach {
                val resolved = it.resolve()
                if (resolved is PsiFile) {
                    return resolved
                }
            }
        } else {
            fail("No tag directive found at offset ${myFixture.caretOffset}")
        }

        return null
    }

    @Test fun testRefToFileInSameDir() {
        val file = initFile("file1.peb", "file2.peb")

        moveCaret(12, 3)

        val referenced = findReferencedFile(file)

        if (referenced != null) {
            assertEquals("file2.peb", referenced.name)
        } else {
            assertNotNull(referenced)
        }
    }

    @Test fun testRefToFileInSubDir() {
        val file = initFile("file2.peb", "subdir/file3.peb")

        moveCaret(12, 3)

        val referenced = findReferencedFile(file)

        if (referenced != null) {
            assertEquals("file3.peb", referenced.name)
        } else {
            assertNotNull(referenced)
        }
    }

    @Test fun testRefToFileInParentDir() {
        val file = initFile("subdir/file3.peb", "file1.peb")

        moveCaret(12, 3)

        val referenced = findReferencedFile(file)

        if (referenced != null) {
            assertEquals("file1.peb", referenced.name)
        } else {
            assertNotNull(referenced)
        }
    }
}