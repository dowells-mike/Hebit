import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Category } from '../models';
import { AuthRequest } from '../types';

/**
 * @desc    Get all categories for a user
 * @route   GET /api/categories
 * @access  Private
 */
export const getCategories = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Build the filter object
  const filter: any = { user: userId };
  
  // Add filters based on query parameters
  if (req.query.type) {
    filter.type = req.query.type;
  }

  if (req.query.isDefault) {
    filter.isDefault = req.query.isDefault === 'true';
  }
  
  // Execute the query with filters and sort by order
  const categories = await Category.find(filter).sort({ order: 1, name: 1 });
  
  res.status(200).json(categories);
});

/**
 * @desc    Get a category by ID
 * @route   GET /api/categories/:id
 * @access  Private
 */
export const getCategoryById = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const categoryId = req.params.id;
  
  const category = await Category.findOne({ _id: categoryId, user: userId });
  
  if (!category) {
    throw new AppError('Category not found', 404);
  }
  
  res.status(200).json(category);
});

/**
 * @desc    Create a new category
 * @route   POST /api/categories
 * @access  Private
 */
export const createCategory = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!req.body.name) {
    throw new AppError('Name is required', 400);
  }

  /* // Temporarily commented out to allow default 'all'
  if (!req.body.type) {
    throw new AppError('Type is required', 400);
  }
  */
  
  // Find the highest order value for the user's categories
  const highestOrder = await Category.findOne({ user: userId })
    .sort({ order: -1 })
    .select('order');
  
  const order = highestOrder ? highestOrder.order + 1 : 0;
  
  const category = await Category.create({
    ...req.body,
    user: userId,
    order
  });
  
  res.status(201).json(category);
});

/**
 * @desc    Update a category
 * @route   PUT /api/categories/:id
 * @access  Private
 */
export const updateCategory = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const categoryId = req.params.id;
  
  const category = await Category.findOne({ _id: categoryId, user: userId });
  
  if (!category) {
    throw new AppError('Category not found', 404);
  }
  
  // Don't allow changing user
  delete req.body.user;
  
  // Update the category
  const updatedCategory = await Category.findByIdAndUpdate(
    categoryId,
    req.body,
    { new: true, runValidators: true }
  );
  
  res.status(200).json(updatedCategory);
});

/**
 * @desc    Delete a category
 * @route   DELETE /api/categories/:id
 * @access  Private
 */
export const deleteCategory = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const categoryId = req.params.id;
  
  const category = await Category.findOne({ _id: categoryId, user: userId });
  
  if (!category) {
    throw new AppError('Category not found', 404);
  }
  
  // Check if it's a default category
  if (category.isDefault) {
    throw new AppError('Cannot delete default categories', 400);
  }
  
  await Category.findByIdAndDelete(categoryId);
  
  res.status(200).json({ success: true });
});

/**
 * @desc    Update category order
 * @route   PATCH /api/categories/reorder
 * @access  Private
 */
export const reorderCategories = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Expect an array of { id, order } objects
  if (!req.body.categories || !Array.isArray(req.body.categories)) {
    throw new AppError('Categories array is required', 400);
  }
  
  const updates = req.body.categories.map(({ id, order }: { id: string, order: number }) => ({
    updateOne: {
      filter: { _id: id, user: userId },
      update: { $set: { order } }
    }
  }));
  
  await Category.bulkWrite(updates);
  
  // Return the updated categories
  const categories = await Category.find({ user: userId }).sort({ order: 1, name: 1 });
  
  res.status(200).json(categories);
}); 