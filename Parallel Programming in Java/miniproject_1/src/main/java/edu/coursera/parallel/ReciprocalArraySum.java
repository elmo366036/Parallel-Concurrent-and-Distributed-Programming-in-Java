package edu.coursera.parallel;

import java.util.concurrent.RecursiveAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Class wrapping methods for implementing reciprocal array sum in parallel.
 */
public final class ReciprocalArraySum {

    /**
     * Default constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Sequentially compute the sum of the reciprocal values for a given array.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        // Compute sum of reciprocals of array elements
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }
        return sum;
    }

    /**
     * Computes the size of each chunk, given the number of chunks to create
     * across a given number of elements.
     *
     * @param nChunks The number of chunks to create
     * @param nElements The number of elements to chunk across
     * @return The default chunk size
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Computes the inclusive element index that the provided chunk starts at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the start of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The inclusive index that this chunk starts at in the set of
     *         nElements
     */
    //starting point of chunk based on the size of the chunk and the chunk number
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Computes the exclusive element index that the provided chunk ends at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the end of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The exclusive end index for this chunk
     */
    //ending point of a chunk based on the size of the chink and the chunk number
    //checks to see if the end is beyond the number of elements
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * This class stub can be filled in to implement the body of each task
     * created to perform reciprocal array sum in parallel.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
        /**
         * Starting index for traversal done by this task.
         */
        private final int startIndexInclusive;
        /**
         * Ending index for traversal done by this task.
         */
        private final int endIndexExclusive;
        /**
         * Input array to reciprocal sum.
         */
        private final double[] input;
        /**
         * Intermediate value produced by this task.
         */
        private double value;

        private long SEQUENTIAL_THRESHOLD = 10000;
        
        /**
         * Constructor.
         * @param setStartIndexInclusive Set the starting index to begin
         *        parallel traversal at.
         * @param setEndIndexExclusive Set ending index for parallel traversal.
         * @param setInput Input values
         */
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        /**
         * Getter for the value produced by this task.
         * @return Value produced by this task
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {	//compute() overrides an abstract method
        							//it is not directly called.
        							//instead, called by fork or invoke
        	//if the indexes are close enough together, do it serially
        	if (endIndexExclusive - startIndexInclusive <= SEQUENTIAL_THRESHOLD) {
            	for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
            		value += 1 / input[i];
            	}
            }
            else {//else, run divide an conquer parallel recursion
            	int mid = (endIndexExclusive + startIndexInclusive) / 2;
            	ReciprocalArraySumTask left = new ReciprocalArraySumTask(startIndexInclusive, mid, input);
            	ReciprocalArraySumTask right = new ReciprocalArraySumTask(mid, endIndexExclusive, input);
            	invokeAll(left, right); //this calls compute for left and right
            	value = left.getValue()  + right.getValue();   	           	
            }
        }
    }

    /**
     * TODO: Modify this method to compute the same reciprocal sum as
     * seqArraySum, but use two tasks running in parallel under the Java Fork
     * Join framework. You may assume that the length of the input array is
     * evenly divisible by 2.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double parArraySum(final double[] input) { //num tasks = 2
        assert input.length % 2 == 0;
        //System.out.println(Runtime.getRuntime().availableProcessors()); //says 8 cores, there are only 4
        return parManyTaskArraySum(input, 2); //runs parManyTaskArraySum with two cores             
    }

    /**
     * TODO: Extend the work you did to implement parArraySum to use a set
     * number of tasks to compute the reciprocal array sum. You may find the
     * above utilities getChunkStartInclusive and getChunkEndExclusive helpful
     * in computing the range of element indices that belong to each chunk.
     *
     * @param input Input array
     * @param numTasks The number of tasks to create
     * @return The sum of the reciprocals of the array input
     */
    protected static double parManyTaskArraySum(final double[] input,
            final int numTasks) {
       double sum = 0;
       //System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4"); //this doesn't seem to matter
       //ForkJoinPool pool = new ForkJoinPool(numTasks); //not even used
       
       List<ReciprocalArraySumTask> tasks = new ArrayList<>();
       
       //create numTask ReciptocalArraySumTasks and put in tasks;    
       //for each task, create a ReciprocalArraySumTask using getChunk helper methods for the indexes
       
       for (int i = 0; i < numTasks; i++) {
    	   tasks.add(new ReciprocalArraySumTask(
    			   getChunkStartInclusive(i, numTasks, input.length), 	//start index
    			   getChunkEndExclusive(i, numTasks, input.length),		//end index
    			   input));
       }
       
       ReciprocalArraySumTask.invokeAll(tasks); 	//calls compute() on all ReciprocalArraySumTasks in parallel
        
       for (ReciprocalArraySumTask task: tasks) { 	//sum up the values for each task
           sum += task.getValue();
       }

        return sum;
    }
}
