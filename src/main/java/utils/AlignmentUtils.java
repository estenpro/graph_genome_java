package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import configuration.Configuration;
import data.Alignment;
import data.Graph;
import data.Node;

public class AlignmentUtils {
  /**
   * PO-MSA
   */
  public static Alignment align(Graph g, String sequence, Configuration configuration) {
    return alignRegion(g, g.getHead(), g.getTail(), sequence, configuration);
  }

  public static Alignment alignRegion(Graph g, Node start, Node end, String sequence,
      Configuration configuration) {
    LogUtils.printInfo("Brute force aligning sequence " + sequence);

    final int HORIZONTAL = 0;
    final int DIAGONAL = 1;
    final int VERTICAL = 2;

    long startTime = System.nanoTime();
    char[] characters = sequence.toCharArray();
    List<Node> queue = new ArrayList<Node>();
    Set<Integer> waiting = new HashSet<Integer>();
    Map<Integer, Integer[]> results = new HashMap<Integer, Integer[]>();
    Map<Integer, Integer[]> backPointers = new HashMap<Integer, Integer[]>();
    Map<Integer, Boolean[]> gaps = new HashMap<Integer, Boolean[]>();
    Map<Integer, Integer[]> path = new HashMap<Integer, Integer[]>();

    int max = Integer.MIN_VALUE;
    int lastNode = -1;

    Integer[] values = new Integer[characters.length + 1];
    Boolean[] firstGaps = new Boolean[characters.length + 1];
    values[0] = 0;
    values[1] = values[0] - configuration.getGapOpeningPenalty();
    firstGaps[1] = false;
    for (int i = 2; i < values.length; i++) {
      values[i] = values[i - 1] - configuration.getGapExtensionPenalty();
      firstGaps[i] = false;
    }

    results.put(start.getIndex(), values);
    gaps.put(start.getIndex(), firstGaps);
    for (Integer neighbour : start.getOutgoing()) {
      queue.add(g.getNode(neighbour));
      waiting.add(neighbour);
    }

    // Iterates over all vertices
    while (!queue.isEmpty()) {
      Node curr = queue.remove(0);
      values = new Integer[characters.length + 1];
      values[0] = 0;
      for (int i = 1; i < values.length; i++) {
        values[i] = Integer.MIN_VALUE;
      }
      Boolean[] myGaps = new Boolean[characters.length + 1];
      Integer[] myBackPointers = new Integer[characters.length + 1];
      Integer[] paths = new Integer[characters.length + 1];
      boolean wait = false;

      // Iterates over all incoming paths to a vertex
      for (Integer neighbour : curr.getIncoming()) {
        Integer[] prev = results.get(neighbour);
        Boolean[] prevGaps = gaps.get(neighbour);
        if (prev == null) {
          wait = true;
          break;
        }

        // Iterates over all indexes of the string in one preceding vertex
        for (int i = 1; i < values.length; i++) {
          int verticalScore = values[i - 1];
          if (i == 1 || values[i - 1] - values[i - 2] == configuration.getGapExtensionPenalty()
              || values[i - 1] - values[i - 2] == configuration.getGapOpeningPenalty()) {
            verticalScore -= configuration.getGapExtensionPenalty();
          } else {
            verticalScore -= configuration.getGapOpeningPenalty();
          }
          int horizontalScore = prev[i];
          if (prevGaps[i]) {
            horizontalScore -= configuration.getGapExtensionPenalty();
          } else {
            horizontalScore -= configuration.getGapOpeningPenalty();
          }
          int diagonalScore =
              prev[i - 1] + configuration.getScore(characters[i - 1], curr.getValue());
          int myMax = Math.max(verticalScore, Math.max(horizontalScore, diagonalScore));

          // Finds the highest score and sets backpointer and gap
          if (myMax > values[i]) {
            values[i] = myMax;
            if (myMax == horizontalScore) {
              myGaps[i] = true;
              myBackPointers[i] = HORIZONTAL;
              paths[i] = neighbour;
            } else if (myMax == diagonalScore) {
              myGaps[i] = false;
              myBackPointers[i] = DIAGONAL;
              paths[i] = neighbour;
            } else {
              myGaps[i] = false;
              myBackPointers[i] = VERTICAL;
            }
          }
        }
      }
      if (wait) {
        queue.add(curr);
        continue;
      }
      for (Integer neighbour : curr.getOutgoing()) {
        if (!waiting.contains(neighbour)) {
          queue.add(g.getNode(neighbour));
          waiting.add(neighbour);
        }
      }
      results.put(curr.getIndex(), values);
      gaps.put(curr.getIndex(), myGaps);
      backPointers.put(curr.getIndex(), myBackPointers);
      path.put(curr.getIndex(), paths);
      waiting.remove(curr.getIndex());
      if (values[values.length - 1] > max) {
        max = values[values.length - 1];
        lastNode = curr.getIndex();
      }
    }

    // Backtracks the alignment
    int index = characters.length;
    int[] alignmentSequence = new int[sequence.length()];
    while (index > 0) {
      int backPointer = backPointers.get(lastNode)[index];
      if (backPointer == DIAGONAL) {
        alignmentSequence[index - 1] = lastNode;
        lastNode = path.get(lastNode)[index];
        index--;
      } else if (backPointer == VERTICAL) {
        alignmentSequence[index - 1] = 0;
        index--;
      } else {
        lastNode = path.get(lastNode)[index];
      }
    }

    long time = System.nanoTime() - startTime;
    Alignment alignment = new Alignment();
    alignment.setScore(max);
    alignment.setTime(time);
    alignment.setType("Brute force");
    alignment.setSequenceLength(characters.length);
    alignment.setGraphSize(g.getCurrentSize());
    alignment.setAlignment(alignmentSequence);

    return alignment;
  }
}
