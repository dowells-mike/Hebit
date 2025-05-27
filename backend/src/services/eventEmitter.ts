import EventEmitter from 'events';

// Create a new EventEmitter instance
const eventEmitter = new EventEmitter();

// Optional: Increase the maximum number of listeners if needed
// eventEmitter.setMaxListeners(20); // Default is 10

export default eventEmitter; 